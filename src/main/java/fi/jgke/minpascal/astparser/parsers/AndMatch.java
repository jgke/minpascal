package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.util.Pair;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class AndMatch implements Parser {
    private final String name;
    @Getter
    private final List<Parser> parsers;
    private static final Parser whitespace = new RuleMatch("whitespace");

    public AndMatch(String name, List<Parser> parsers) {
        this.name = name;
        this.parsers = parsers;
    }

    public String getName() {
        return name;
    }


    @Override
    public Pair<AstNode, Pair<String, Position>> parse(Pair<String, Position> str) {
        Position pos = str.getRight();
        List<AstNode> nodes = new ArrayList<>();
        for (Parser p : parsers) {
            str = whitespace.parse(str).getRight();
            Pair<AstNode, Pair<String, Position>> pair = p.parse(str);
            nodes.add(pair.getLeft());
            str = pair.getRight();
        }
        return new Pair<>(new ListAstNode(name, nodes, pos), str);
    }

    @Override
    public boolean parses(String str) {
        return parsers.get(0).parses(str);
    }

    @Override
    public String toString() {
        String s = parsers.toString();
        return name + '{' + s.substring(1, s.length() - 1) + '}';
    }
}
