package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.util.Pair;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class JustMatch implements Parser {
    private final List<Parser> parsers;
    private final String name;
    private static final Parser whitespace = new RuleMatch("whitespace");

    @Override
    public Pair<AstNode, String> parse(String str) {
        List<AstNode> nodes = new ArrayList<>();
        for (Parser p : parsers) {
            str = whitespace.parse(str).getRight();
            Pair<AstNode, String> pair = p.parse(str);
            nodes.add(pair.getLeft());
            str = pair.getRight();
        }
        return new Pair<>(new ListAstNode(name, nodes), str);
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
