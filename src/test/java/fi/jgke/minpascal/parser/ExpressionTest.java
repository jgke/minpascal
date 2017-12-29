package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.expressions.Expression;
import fi.jgke.minpascal.parser.nodes.ExpressionNode;
import fi.jgke.minpascal.parser.nodes.LiteralNode;
import fi.jgke.minpascal.parser.nodes.SimpleExpressionNode;
import org.junit.Test;

import java.util.Optional;

import static fi.jgke.minpascal.TestUtils.queueWith;
import static fi.jgke.minpascal.data.TokenType.INTEGER_LITERAL;
import static fi.jgke.minpascal.data.TokenType.PLUS;
import static fi.jgke.minpascal.data.TokenType.TIMES;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@SuppressWarnings("ConstantConditions")
public class ExpressionTest {

    private final Token five = new Token(INTEGER_LITERAL, Optional.of(5), new Position(0, 0));
    private final Token six = new Token(INTEGER_LITERAL, Optional.of(6), new Position(0, 0));
    private final Token plus = new Token(PLUS, Optional.empty(), new Position(0, 0));
    private final Token times = new Token(TIMES, Optional.empty(), new Position(0, 0));

    @Test
    public void simpleLiteral() {
        ParseQueue queue = queueWith(five);
        ExpressionNode parse = new Expression().parse(queue);
        SimpleExpressionNode node = parse.getLeft();
        assertThat("No sign is present", !node.getSign().isPresent());
        assertThat("No operator is present", !node.getAddingOperator().isPresent());
        assertThat("No right is present", !node.getRight().isPresent());
        LiteralNode leftLiteral = node.getLeft().getLeft().getLiteral().get();
        assertThat("Left equals five", leftLiteral.getInteger().get(), is(equalTo(5)));
    }

    @Test
    public void withAddingOperator() {
        ParseQueue queue = queueWith(five, plus, six);
        ExpressionNode parse = new Expression().parse(queue);
        SimpleExpressionNode node = parse.getLeft();
        assertThat("No sign is present", !node.getSign().isPresent());
        LiteralNode leftLiteral = node.getLeft().getLeft().getLiteral().get();
        LiteralNode rightLiteral = node.getRight().get().getLeft().getLiteral().get();
        assertThat("Left equals five", leftLiteral.getInteger().get(), is(equalTo(5)));
        assertThat("Operator is plus", node.getAddingOperator().equals(Optional.of(plus)));
        assertThat("Right equals six", rightLiteral.getInteger().get(), is(equalTo(6)));
    }

    @Test
    public void withMultiplyingOperator() {
        ParseQueue queue = queueWith(five, times, six);
        ExpressionNode parse = new Expression().parse(queue);
        SimpleExpressionNode node = parse.getLeft();
        LiteralNode leftLiteral = node.getLeft().getLeft().getLiteral().get();
        LiteralNode rightLiteral = node.getLeft().getRight().get().getLiteral().get();
        assertThat("Left equals five", leftLiteral.getInteger().get(), is(equalTo(5)));
        assertThat("Operator is times", node.getLeft().getOperator().equals(Optional.of(times)));
        assertThat("Right equals six", rightLiteral.getInteger().get(), is(equalTo(6)));
    }
}
