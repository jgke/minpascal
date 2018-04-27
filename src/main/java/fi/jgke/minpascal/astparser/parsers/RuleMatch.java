package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.AstParser;
import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.util.Pair;
import lombok.AllArgsConstructor;

import java.util.Collections;

@AllArgsConstructor
public class RuleMatch implements Parser {
    private final String rule;

    @Override
    public Pair<AstNode, String> parse(String str) {
        Pair<AstNode, String> parse = AstParser.rules.get(rule).parse(str);
        if (Character.isLowerCase(rule.charAt(0))) {
            parse.getLeft().setName(rule);
        }
        if(parse.getLeft().getName().equals(rule))
            return parse;
        return new Pair<>(new ListAstNode(rule, Collections.singletonList(parse.getLeft())), parse.getRight());
    }

    @Override
    public boolean parses(String str) {
        return AstParser.rules.get(rule).parses(str);
    }

    @Override
    public String getName() {
        return rule;
    }

    @Override
    public String toString() {
        return rule;
    }
}
