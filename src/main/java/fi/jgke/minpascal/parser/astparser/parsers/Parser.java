package fi.jgke.minpascal.parser.astparser.parsers;

import fi.jgke.minpascal.parser.astparser.nodes.AstNode;
import fi.jgke.minpascal.util.Pair;

public interface Parser {
    Pair<AstNode, String> parse(String str);
    boolean parses(String str);
}
