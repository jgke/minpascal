package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.expressions.Expression;
import fi.jgke.minpascal.parser.nodes.AssertNode;
import fi.jgke.minpascal.parser.nodes.ExpressionNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class Assert implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(ASSERT);
    }

    @Override
    public AssertNode parse(ParseQueue queue) {
        queue.getExpectedTokens(ASSERT, OPENPAREN);
        ExpressionNode booleanExpr = new Expression().parse(queue);
        queue.getExpectedTokens(CLOSEPAREN);
        return new AssertNode(booleanExpr);
    }
}
