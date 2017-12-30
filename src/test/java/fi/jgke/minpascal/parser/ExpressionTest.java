package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.expressions.Expression;
import fi.jgke.minpascal.parser.nodes.ExpressionNode;
import fi.jgke.minpascal.parser.nodes.LiteralNode;
import fi.jgke.minpascal.parser.nodes.SimpleExpressionNode;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.jgke.minpascal.TestUtils.queueWith;
import static fi.jgke.minpascal.data.TokenType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@SuppressWarnings("ConstantConditions")
public class ExpressionTest {

    private final Token five = new Token(INTEGER_LITERAL, Optional.of(5), new Position(0, 0));
    private final Token six = new Token(INTEGER_LITERAL, Optional.of(6), new Position(0, 0));
    private final Token plus = new Token(PLUS, Optional.empty(), new Position(0, 0));
    private final Token times = new Token(TIMES, Optional.empty(), new Position(0, 0));
    private final Token dot = new Token(DOT, Optional.empty(), new Position(0, 0));
    private final Token size = new Token(IDENTIFIER, Optional.of("size"), new Position(0, 0));
    private final Token not = new Token(NOT, Optional.empty(), new Position(0, 0));
    private final Token op = new Token(OPENPAREN, Optional.empty(), new Position(0, 0));
    private final Token cp = new Token(CLOSEPAREN, Optional.empty(), new Position(0, 0));
    private final Token comma = new Token(COMMA, Optional.empty(), new Position(0, 0));
    private final Token id = new Token(IDENTIFIER, Optional.of("foobar"), new Position(0, 0));
    private final Token ob = new Token(OPENBRACKET, Optional.empty(), new Position(0, 0));
    private final Token cb = new Token(CLOSEBRACKET, Optional.empty(), new Position(0, 0));

    @Test
    public void literal() {
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

    @Test
    public void literalWithSign() {
        ParseQueue queue = queueWith(plus, five);
        ExpressionNode parse = new Expression().parse(queue);
        SimpleExpressionNode node = parse.getLeft();
        assertThat("Sign is plus", node.getSign().equals(Optional.of(plus)));
        LiteralNode leftLiteral = node.getLeft().getLeft().getLiteral().get();
        assertThat("Left equals five", leftLiteral.getInteger().get(), is(equalTo(5)));
    }

    @Test
    public void literalWithSizeExpression() {
        ParseQueue queue = queueWith(five, dot, size);
        ExpressionNode parse = new Expression().parse(queue);
        assertThat("Size expression is present",
                parse.getLeft().getLeft().getLeft().getSizeExpression().isPresent());
        assertThat("Inner size expression is not present",
                !parse.getLeft().getLeft().getLeft().getSizeExpression().get().getSizeExpression().isPresent());
    }

    @Test
    public void literalWithTwoSizeExpressions() {
        ParseQueue queue = queueWith(five, dot, size, dot, size);
        ExpressionNode parse = new Expression().parse(queue);
        assertThat("Size expression is present",
                parse.getLeft().getLeft().getLeft().getSizeExpression().isPresent());
        assertThat("Inner size expression is present",
                parse.getLeft().getLeft().getLeft().getSizeExpression().get().getSizeExpression().isPresent());
    }

    @Test
    public void notExpression() {
        ParseQueue queue = queueWith(not, five);
        ExpressionNode parse = new Expression().parse(queue);
        assertThat("Not expression is present", parse.getLeft().getLeft().getLeft().getNot().isPresent());
        assertThat("Not contains five",
                parse.getLeft().getLeft().getLeft().getNot().get().getFactor().getLiteral().get().getInteger().get(),
                is(equalTo(5)));
    }

    @Test
    public void parenExpression() {
        ParseQueue queue = queueWith(op, five, cp);
        ExpressionNode parse = new Expression().parse(queue);
        assertThat("Paren expression is present", parse.getLeft().getLeft().getLeft().getExpression().isPresent());
        assertThat("Expression contains five",
                parse.getLeft().getLeft().getLeft().getExpression().get().getLeft().getLeft().getLeft().getLiteral().get().getInteger().get(),
                is(equalTo(5)));
    }

    @Test
    public void callExpression() {
        ParseQueue queue = queueWith(id, op, cp);
        ExpressionNode parse = new Expression().parse(queue);
        assertThat("Call expression is present", parse.getLeft().getLeft().getLeft().getCall().isPresent());
        assertThat("Call identifier contains foobar",
                parse.getLeft().getLeft().getLeft().getCall().get().getIdentifier().getValue().get(),
                is(equalTo("foobar")));
    }

    @Test
    public void callWithArgument() {
        ParseQueue queue = queueWith(id, op, five, cp);
        ExpressionNode parse = new Expression().parse(queue);
        assertThat("Call expression is present", parse.getLeft().getLeft().getLeft().getCall().isPresent());
        assertThat("Call identifier contains foobar",
                parse.getLeft().getLeft().getLeft().getCall().get().getIdentifier().getValue().get(),
                is(equalTo("foobar")));
        assertThat("Call arguments contain 5",
                parse.getLeft().getLeft().getLeft().getCall().get().getArguments().getArguments().stream()
                        .map(e -> e.getLeft().getLeft().getLeft().getLiteral().get().getInteger().get()).collect(Collectors.toList()),
                is(equalTo(Collections.singletonList(5))));
    }

    @Test
    public void callWithMultipleArguments() {
        ParseQueue queue = queueWith(id, op, five, comma, six, comma, five, cp);
        ExpressionNode parse = new Expression().parse(queue);
        assertThat("Call expression is present", parse.getLeft().getLeft().getLeft().getCall().isPresent());
        assertThat("Call identifier contains foobar",
                parse.getLeft().getLeft().getLeft().getCall().get().getIdentifier().getValue().get(),
                is(equalTo("foobar")));
        assertThat("Call arguments contain 5, 6, 5",
                parse.getLeft().getLeft().getLeft().getCall().get().getArguments().getArguments().stream()
                        .map(e -> e.getLeft().getLeft().getLeft().getLiteral().get().getInteger().get()).collect(Collectors.toList()),
                is(equalTo(Arrays.asList(5, 6, 5))));
    }

    @Test
    public void variableExpression() {
        ParseQueue queue = queueWith(id);
        ExpressionNode parse = new Expression().parse(queue);
        assertThat("Variable is present", parse.getLeft().getLeft().getLeft().getVariable().isPresent());
        assertThat("Variable contains foobar",
                parse.getLeft().getLeft().getLeft().getVariable().get().getIdentifier(),
                is(equalTo(id)));
    }

    @Test
    public void variableWithArrayAccess() {
        ParseQueue queue = queueWith(id, ob, five, cb);
        ExpressionNode parse = new Expression().parse(queue);
        assertThat("Variable is present", parse.getLeft().getLeft().getLeft().getVariable().isPresent());
        assertThat("Variable contains foobar",
                parse.getLeft().getLeft().getLeft().getVariable().get().getIdentifier(),
                is(equalTo(id)));
        assertThat("Integer is present",
                parse.getLeft().getLeft().getLeft().getVariable().get().getArrayAccessInteger().isPresent());
        assertThat("Integer equals five",
                parse.getLeft().getLeft().getLeft().getVariable().get().getArrayAccessInteger()
                        .get().getLeft().getLeft().getLeft().getLiteral().get().getInteger().get(),
                is(equalTo(5)));
    }
}
