package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.util.Pair;
import lombok.AllArgsConstructor;

import java.util.*;

@AllArgsConstructor
public class KleeneMatch implements Parser {
    private final static String name = "more";
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
        ListAstNode listAstNode = new ListAstNode(name, pair.getLeft());
        List<String> strings = Arrays.asList(name, parser.getName());
        listAstNode.setAvailableNames(new HashSet<>(strings));
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
