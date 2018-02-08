package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.expressions.Expression;
import fi.jgke.minpascal.parser.expressions.Variable;
import fi.jgke.minpascal.parser.nodes.AssignmentNode;
import fi.jgke.minpascal.parser.nodes.ExpressionNode;
import fi.jgke.minpascal.parser.nodes.VariableNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.ASSIGN;

public class AssignmentStatement implements Parsable {
    private static final Variable variable = new Variable();

    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(variable);
    }

    @Override
    public AssignmentNode parse(ParseQueue queue) {
        throw new CompilerException("parseWithIdentifier() called on AssignmentStatement without an identifier");
    }

    public AssignmentNode parseWithIdentifier(Token<String> identifier, ParseQueue queue) {
        VariableNode left = variable.parseWithIdentifier(identifier, queue);
        queue.getExpectedToken(ASSIGN);
        ExpressionNode right = new Expression().parse(queue);
        return new AssignmentNode(left, right);
    }
}
