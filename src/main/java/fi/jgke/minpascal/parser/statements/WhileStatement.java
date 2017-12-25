package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.blocks.Statement;
import fi.jgke.minpascal.parser.expressions.Expression;
import fi.jgke.minpascal.parser.nodes.ExpressionNode;
import fi.jgke.minpascal.parser.nodes.WhileNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.DO;
import static fi.jgke.minpascal.data.TokenType.WHILE;

public class WhileStatement implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(WHILE);
    }

    @Override
    public WhileNode parse(ParseQueue queue) {
        queue.getExpectedTokens(WHILE);
        ExpressionNode condition = new Expression().parse(queue);
        queue.getExpectedTokens(DO);
        TreeNode statement = new Statement().parse(queue);
        return new WhileNode(condition, statement);
    }
}
