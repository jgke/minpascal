package fi.jgke.minpascal.parser.expressions;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.base.ParseUtils;
import fi.jgke.minpascal.parser.nodes.ExpressionNode;
import fi.jgke.minpascal.parser.nodes.SimpleExpressionNode;

import java.util.Collections;
import java.util.List;

public class Expression implements Parsable {
    private SimpleExpression simpleExpression = new SimpleExpression();

    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(simpleExpression);
    }

    @Override
    public ExpressionNode parse(ParseQueue queue) {
        SimpleExpressionNode left = simpleExpression.parse(queue);
        if (!queue.isNext(ParseUtils.relationalOperators)) {
            return new ExpressionNode(left);
        }
        Token operator = queue.getExpectedToken(ParseUtils.relationalOperators);
        SimpleExpressionNode right = simpleExpression.parse(queue);
        return new ExpressionNode(left, operator, right);
    }
}
