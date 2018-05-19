package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.AstParser;
import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.util.Pair;
import lombok.AllArgsConstructor;

import java.util.Collections;

@AllArgsConstructor
public class RuleParser implements Parser {
    private final String rule;

    @Override
    public Pair<AstNode, Pair<String, Position>> parse(Pair<String, Position> str) {
        Pair<AstNode, Pair<String, Position>> parse = AstParser.getRules().get(rule).parse(str);
        if (Character.isLowerCase(rule.charAt(0))) {
            parse.getLeft().setName(rule);
        }
        if(parse.getLeft().getName().equals(rule))
            return parse;
        ListAstNode listAstNode = new ListAstNode(rule, Collections.singletonList(parse.getLeft()), str.getRight());
        listAstNode.setAvailableNames(parse.getLeft().getAvailableNames());
        return new Pair<>(listAstNode, parse.getRight());
    }

    @Override
    public boolean parses(String str) {
        return AstParser.getRules().get(rule).parses(str);
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
