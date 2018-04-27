package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class KleeneMatch implements Parser {
    @Getter
    private final String name;
    private final Parser parser;

    @Override
    public Pair<AstNode, String> parse(String str) {
        Pair<List<AstNode>, String> pair = new Pair<>(Collections.emptyList(), str);
        while (parser.parses(pair.getRight())) {
            Pair<AstNode, String> parse = parser.parse(pair.getRight());
            ArrayList<AstNode> astNodes = new ArrayList<>(pair.getLeft());
            astNodes.add(parse.getLeft());
            pair = new Pair<>(astNodes, parse.getRight());
        }
        return new Pair<>(new ListAstNode(name, pair.getLeft()), pair.getRight());
    }

    @Override
    public boolean parses(String str) {
        return true;
    }

    @Override
    public String toString() {
        return "(" + parser + ")*";
    }
}
