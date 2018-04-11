package fi.jgke.minpascal.parser.astparser.parsers;

import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.astparser.nodes.AstNode;
import fi.jgke.minpascal.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OrMatch implements Parser {
    private final List<Parser> parsers;

    public OrMatch(List<Parser> parsers) {
        if (parsers.size() > 1) {
            Parser maybeOr = parsers.get(1);
            if (maybeOr instanceof OrMatch) {
                List<Parser> newParsers = new ArrayList<>(((OrMatch) maybeOr).parsers);
                newParsers.add(0, parsers.get(0));
                parsers = newParsers;
            }
        }
        this.parsers = parsers;
    }

    private static Supplier<CompilerException> parseFailure(List<Parser> parsers) {
        return () -> {
            throw new CompilerException("Parse failure, expected any of " + parsers);
        };
    }

    @Override
    public Pair<AstNode, String> parse(String str) {
        for (Parser p : parsers) {
            if (p.parses(str)) {
                return p.parse(str);
            }
        }
        throw parseFailure(parsers).get();
    }

    @Override
    public boolean parses(String str) {
        return parsers.stream().anyMatch(parser -> parser.parses(str));
    }

    @Override
    public String toString() {
        return parsers.toString();
    }
}
