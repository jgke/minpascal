package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.util.Pair;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class KleeneParser implements Parser {
    private final static String name = "more";
    private final Parser parser;

    @Override
    public Pair<AstNode, Pair<String, Position>> parse(Pair<String, Position> str) {
        Position p = str.getRight();
        Pair<List<AstNode>, Pair<String, Position>> pair = new Pair<>(Collections.emptyList(), str);
        while (parser.parses(pair.getRight().getLeft())) {
            Pair<AstNode, Pair<String, Position>> parse = parser.parse(pair.getRight());
            ArrayList<AstNode> astNodes = new ArrayList<>(pair.getLeft());
            astNodes.add(parse.getLeft());
            pair = new Pair<>(astNodes, parse.getRight());
        }
        ListAstNode listAstNode = new ListAstNode(name, pair.getLeft(), p);
        return new Pair<>(listAstNode, pair.getRight());
    }

    @Override
    public boolean parses(String str) {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "(" + parser + ")*";
    }
}
