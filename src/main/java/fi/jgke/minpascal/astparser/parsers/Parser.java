package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.util.Pair;

public interface Parser {
    Pair<AstNode, String> parse(String str);
    boolean parses(String str);
    String getName();
}
