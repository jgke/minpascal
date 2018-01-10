package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.blocks.Statement;
import fi.jgke.minpascal.parser.expressions.Expression;
import fi.jgke.minpascal.parser.nodes.ExpressionNode;
import fi.jgke.minpascal.parser.nodes.IfThenNode;
import fi.jgke.minpascal.parser.nodes.StatementNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class IfStatement implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(IF);
    }

    @Override
    public IfThenNode parse(ParseQueue queue) {
        queue.getExpectedToken(IF);
        ExpressionNode condition = new Expression().parse(queue);
        queue.getExpectedToken(THEN);
        StatementNode thenStatement = new Statement().parse(queue);
        if(queue.isNext(ELSE)) {
            queue.getExpectedToken(ELSE);
            StatementNode elseStatement = new Statement().parse(queue);
            return new IfThenNode(condition, thenStatement, elseStatement);
        }
        return new IfThenNode(condition, thenStatement);
    }
}
