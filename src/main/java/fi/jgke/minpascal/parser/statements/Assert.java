package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.expressions.Expression;
import fi.jgke.minpascal.parser.nodes.AssertNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.ASSERT;
import static fi.jgke.minpascal.data.TokenType.CLOSEPAREN;
import static fi.jgke.minpascal.data.TokenType.OPENPAREN;

public class Assert implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(ASSERT);
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        queue.getExpectedTokens(ASSERT, OPENPAREN);
        TreeNode booleanExpr = new Expression().parse(queue);
        queue.getExpectedTokens(CLOSEPAREN);
        return new AssertNode(booleanExpr);
    }
}
