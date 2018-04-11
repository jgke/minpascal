package fi.jgke.minpascal.parser.astparser.parsers;

import fi.jgke.minpascal.parser.astparser.AstParser;
import fi.jgke.minpascal.parser.astparser.nodes.AstNode;
import fi.jgke.minpascal.util.Pair;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RuleMatch implements Parser {
    private final String rule;

    @Override
    public Pair<AstNode, String> parse(String str) {
        return AstParser.rules.get(rule).parse(str);
    }

    @Override
    public boolean parses(String str) {
        return AstParser.rules.get(rule).parses(str);
    }

    @Override
    public String toString() {
        return rule;
    }
}
