package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.expressions.Expression;
import fi.jgke.minpascal.parser.nodes.ArgumentsNode;
import fi.jgke.minpascal.parser.nodes.ExpressionNode;

import java.util.Arrays;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.CLOSEPAREN;

public class Arguments implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(new Expression(), CLOSEPAREN);
    }

    @Override
    public ArgumentsNode parse(ParseQueue queue) {
        Expression e = new Expression();
        List<ExpressionNode> arguments = queue.collectByComma(e::parse);
        return new ArgumentsNode(arguments);
    }
}
