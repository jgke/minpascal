package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.EmptyNode;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.util.Pair;

public class Epsilon implements Parser {
    @Override
    public Pair<AstNode, Pair<String, Position>> parse(Pair<String, Position> str) {
        return new Pair<>(new EmptyNode("_epsilon", str.getRight()), str);
    }

    @Override
    public boolean parses(String str) {
        return true;
    }

    @Override
    public String getName() {
        return "epsilon";
    }

    @Override
    public String toString() {
        return getName();
    }
}
