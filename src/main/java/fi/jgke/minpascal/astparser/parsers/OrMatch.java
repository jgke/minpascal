package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OrMatch implements Parser {
    private String name;
    private final List<Parser> parsers;
    private final Set<String> names;

    public OrMatch(String name, List<Parser> parsers) {
        if (parsers.size() > 1) {
            Parser maybeOr = parsers.get(1);
            if (maybeOr instanceof OrMatch) {
                List<Parser> newParsers = new ArrayList<>(((OrMatch) maybeOr).parsers);
                newParsers.add(0, parsers.get(0));
                parsers = newParsers;
            }
        }
        this.parsers = new ArrayList<>(parsers);
        this.names = parsers.stream()
                .map(Parser::getName)
                .collect(Collectors.toSet());
        this.name = name;
    }

    public void addParserToFront(Parser parser, String newName) {
        parsers.add(0, parser);
        names.add(parser.getName());
        this.name = newName;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    private static CompilerException parseFailure(List<Parser> parsers) {
        return new CompilerException("Parse failure, expected any of " + parsers);
    }

    @Override
    public Pair<AstNode, String> parse(String str) {
        for (Parser p : parsers) {
            if (p.parses(str)) {
                Pair<AstNode, String> parse = p.parse(str);
                ListAstNode listAstNode = new ListAstNode(p.getName(), Collections.singletonList(parse.getLeft()));
                listAstNode.setAvailableNames(names);
                return new Pair<>(listAstNode, parse.getRight());
            }
        }
        throw parseFailure(parsers);
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
