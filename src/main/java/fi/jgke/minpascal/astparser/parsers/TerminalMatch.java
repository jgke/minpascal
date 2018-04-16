package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.util.Regex;
import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.LeafNode;
import fi.jgke.minpascal.util.Pair;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TerminalMatch implements Parser {
    private final String name;
    private final String pattern;
    private final boolean regex;
    private final Regex compiled;

    public TerminalMatch(String name, String pattern, boolean regex) {
        this.name = name;
        this.pattern = pattern;
        this.regex = regex;
        if(regex) {
            this.compiled = new Regex(pattern);
        } else {
            this.compiled = null;
        }
    }

    @Override
    public Pair<AstNode, String> parse(String str) {
        List<String> groups;
        String match;
        String rest;
        try {
            if (regex) {
                assert compiled != null;
                int matchLength = compiled.match(str);
                match = str.substring(0, matchLength);
                rest = str.substring(matchLength);
            } else {
                if (!str.startsWith(this.pattern)) {
                    throw new IllegalArgumentException();
                }
                match = this.pattern;
                rest = str.substring(this.pattern.length());
            }
        } catch (IllegalStateException e) {
            System.out.println("No match when parsing " + name + " (" + this.pattern + ')');
            System.out.println(str);
            throw new CompilerException(e);
        }
        return new Pair<>(new LeafNode(name, match), rest);
    }

    @Override
    public boolean parses(String str) {
        if (regex) {
            return compiled.match(str) != -1;
        } else {
            return str.startsWith(pattern);
        }
    }

    @Override
    public String toString() {
        return pattern;
    }
}
