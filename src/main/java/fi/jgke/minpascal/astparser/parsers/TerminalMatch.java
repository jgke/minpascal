package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.LeafNode;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.util.Pair;
import fi.jgke.minpascal.util.Regex;
import lombok.Getter;

import java.util.Optional;

public class TerminalMatch implements Parser {
    @Getter
    private final String name;
    private final String pattern;
    private final Optional<Regex> compiled;
    private static final Parser whitespace = new RuleMatch("whitespace");

    public TerminalMatch(String name, String pattern, boolean regex) {
        this.name = name;
        this.pattern = pattern;
        if (regex) {
            this.compiled = Optional.of(new Regex(pattern));
        } else {
            this.compiled = Optional.empty();
        }
    }

    @Override
    public Pair<AstNode, String> parse(String str) {
        Pair<AstNode, String> pair;
        if (!this.name.startsWith("_") && !this.name.equals("whitespace")) {
            str = whitespace.parse(str).getRight();
        }
        String finalStr = str;
        pair = this.compiled.<Pair<AstNode, String>>map(regex -> {
            int matchLength = regex.match(finalStr);
            if (matchLength < 0) {
                throw new CompilerException(
                        "Parse error at " + name
                                + ", could not match '" + pattern
                                + "' for string '" + finalStr + "'");
            }
            return new Pair<>(new LeafNode(name,
                    finalStr.substring(0, matchLength)),
                    finalStr.substring(matchLength));
        }).orElseGet(() -> {
            if (!finalStr.startsWith(this.pattern)) {
                throw new CompilerException(
                        "Parse error at " + name
                                + ", could not match '" + pattern
                                + "' for string '" + finalStr + "'");
            }
            return new Pair<>(new LeafNode(name, this.pattern),
                    finalStr.substring(this.pattern.length()));
        });
        str = pair.getRight();
        if (!this.name.startsWith("_") && !this.name.equals("whitespace") &&
                whitespace.parses(pair.getRight())) {
            str = whitespace.parse(pair.getRight()).getRight();
        }
        return new Pair<>(pair.getLeft(), str);
    }

    @Override
    public boolean parses(String str) {
        if (!this.name.startsWith("_") && !this.name.equals("whitespace"))
            str = whitespace.parse(str).getRight();
        String finalStr = str;
        return compiled
                .map(regex -> regex.match(finalStr) != -1)
                .orElse(str.startsWith(pattern));
    }

    @Override
    public String toString() {
        return pattern;
    }
}
