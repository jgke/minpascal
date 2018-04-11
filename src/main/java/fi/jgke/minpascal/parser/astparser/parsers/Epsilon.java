package fi.jgke.minpascal.parser.astparser.parsers;

import fi.jgke.minpascal.parser.astparser.nodes.AstNode;
import fi.jgke.minpascal.parser.astparser.nodes.EmptyNode;
import fi.jgke.minpascal.util.Pair;

public class Epsilon implements Parser {
    @Override
    public Pair<AstNode, String> parse(String str) {
        return new Pair<>(new EmptyNode("_epsilon"), str);
    }

    @Override
    public boolean parses(String str) {
        return true;
    }
}
