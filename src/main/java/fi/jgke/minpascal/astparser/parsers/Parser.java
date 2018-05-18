package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.util.Pair;

public interface Parser {
    Pair<AstNode, Pair<String, Position>> parse(Pair<String, Position> str);
    default Pair<AstNode, Pair<String, Position>> parse(String str) {
        return parse(new Pair<>(str, new Position(1, 1)));
    }
    boolean parses(String str);
    String getName();
}
