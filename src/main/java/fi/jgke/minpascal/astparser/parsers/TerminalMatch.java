package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.LeafNode;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.util.Pair;
import fi.jgke.minpascal.util.Regex;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
public class TerminalMatch implements Parser {
    @Getter
    private final String name;
    private final String pattern;
    private final Optional<Regex> compiled;

    public TerminalMatch(String name, String pattern, boolean regex) {
        this.name = name;
        this.pattern = pattern;
        if(regex) {
            this.compiled = Optional.of(new Regex(pattern));
        } else {
            this.compiled = Optional.empty();
        }
    }

    @Override
    public Pair<AstNode, String> parse(String str) {
        Pair<AstNode, String> pair;
        try {
            pair = this.compiled.<Pair<AstNode, String>>map(regex -> {
                int matchLength = regex.match(str);
                return new Pair<>(new LeafNode(name,
                        str.substring(0, matchLength)),
                        str.substring(matchLength));
            }).orElseGet(() -> {
                if (!str.startsWith(this.pattern)) {
                    throw new IllegalArgumentException();
                }
                return new Pair<>(new LeafNode(name, this.pattern), str.substring(this.pattern.length()));
            });
        } catch (IllegalStateException|StringIndexOutOfBoundsException e) {
            System.out.println("No match when parsing " + name + " (" + this.pattern + ')');
            System.out.println(str);
            throw new CompilerException(e);
        }
        return pair;
    }

    @Override
    public boolean parses(String str) {
        return compiled
                .map(regex -> regex.match(str) != -1)
                .orElse(str.startsWith(pattern));
    }

    @Override
    public String toString() {
        return pattern;
    }
}
