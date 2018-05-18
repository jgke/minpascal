package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.exception.ParseError;
import fi.jgke.minpascal.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OrMatch implements Parser {
    private final List<Parser> parsers;
    private final Set<String> names;

    public OrMatch(List<Parser> parsers) {
        if (parsers.size() > 1) {
            Parser maybeOr = parsers.get(1);
            if (maybeOr instanceof OrMatch) {
                if (parsers.size() != 2) throw new AssertionError();
                List<Parser> newParsers = new ArrayList<>(((OrMatch) maybeOr).parsers);
                newParsers.add(0, parsers.get(0));
                parsers = newParsers;
            }
        }
        this.parsers = new ArrayList<>(parsers);
        this.names = parsers.stream()
                .map(Parser::getName)
                .collect(Collectors.toSet());
    }

    public void addParserToFront(Parser parser) {
        parsers.add(0, parser);
        names.add(parser.getName());
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    private static ParseError parseFailure(List<Parser> parsers, Pair<String, Position> substring) {
        return new ParseError("Parse failure, expected any of " + parsers, substring.getRight());
    }

    @Override
    public Pair<AstNode, Pair<String, Position>> parse(Pair<String, Position> str) {
        Position pos = str.getRight();
        for (Parser p : parsers) {
            if (p.parses(str.getLeft())) {
                Pair<AstNode, Pair<String, Position>> parse = p.parse(str);
                ListAstNode listAstNode = new ListAstNode(p.getName(), Collections.singletonList(parse.getLeft()), pos);
                listAstNode.setAvailableNames(names);
                return new Pair<>(listAstNode, parse.getRight());
            }
        }
        throw parseFailure(parsers, str);
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
