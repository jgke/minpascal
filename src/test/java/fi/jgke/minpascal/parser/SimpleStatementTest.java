package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.SimpleStatementNode;
import fi.jgke.minpascal.parser.statements.SimpleStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static fi.jgke.minpascal.TestUtils.queueWith;
import static fi.jgke.minpascal.data.TokenType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class SimpleStatementTest {

    private final Token<Integer> five = Token.token(INTEGER_LITERAL, 5, new Position(0, 0));
    private final Token<Integer> six = Token.token(INTEGER_LITERAL, 6, new Position(0, 0));
    private final Token<Void> op = Token.token(OPENPAREN, new Position(0, 0));
    private final Token<Void> cp = Token.token(CLOSEPAREN, new Position(0, 0));
    private final Token<Void> comma = Token.token(COMMA, new Position(0, 0));
    private final Token<String> id = Token.token(IDENTIFIER, "foobar", new Position(0, 0));
    private final Token<Void> assign = Token.token(ASSIGN, new Position(0, 0));
    private final Token<Void> returnToken = Token.token(RETURN, new Position(0, 0));
    private final Token<String> read = Token.token(IDENTIFIER, "read", new Position(0, 0));
    private final Token<String> write = Token.token(IDENTIFIER, "writeln", new Position(0, 0));
    private final Token<Void> assertToken = Token.token(ASSERT, new Position(0, 0));

    @Test
    public void assignmentNode() {
        ParseQueue queue = queueWith(id, assign, five);
        SimpleStatementNode parse = new SimpleStatement().parse(queue);
        assertThat("Assignment expression is present", parse.getAssignmentNode().isPresent());
        assertThat("Assignment identifier contains foobar",
                parse.getAssignmentNode().get().getIdentifier().getIdentifier().getValue(),
                is(equalTo("foobar")));
        assertThat("Array assignment is not present",
                !parse.getAssignmentNode().get().getIdentifier().getArrayAccessInteger().isPresent());
        assertThat("Assignment expression contain 5",
                parse.getAssignmentNode().get().getValue().getLeft().getLeft().getLeft().getLiteral().get().getInteger().get(),
                is(equalTo(5)));
    }

    @Test
    public void callStatementWithArgument() {
        ParseQueue queue = queueWith(id, op, five, cp);
        SimpleStatementNode parse = new SimpleStatement().parse(queue);
        assertThat("Call expression is present", parse.getCallNode().isPresent());
        assertThat("Call identifier contains foobar",
                parse.getCallNode().get().getIdentifier().getValue(),
                is(equalTo("foobar")));
        assertThat("Call arguments contain 5",
                parse.getCallNode().get().getArguments().getArguments().stream()
                        .map(e -> e.getLeft().getLeft().getLeft().getLiteral().get().getInteger().get()).collect(Collectors.toList()),
                is(equalTo(Collections.singletonList(5))));
    }

    @Test
    public void callStatementWithMultipleArguments() {
        ParseQueue queue = queueWith(id, op, five, comma, six, comma, five, cp);
        SimpleStatementNode parse = new SimpleStatement().parse(queue);
        assertThat("Call expression is present", parse.getCallNode().isPresent());
        assertThat("Call identifier contains foobar",
                parse.getCallNode().get().getIdentifier().getValue(),
                is(equalTo("foobar")));
        assertThat("Call arguments contain 5, 6, 5",
                parse.getCallNode().get().getArguments().getArguments().stream()
                        .map(e -> e.getLeft().getLeft().getLeft().getLiteral().get().getInteger().get()).collect(Collectors.toList()),
                is(equalTo(Arrays.asList(5, 6, 5))));
    }

    @Test
    public void returnStatement() {
        ParseQueue queue = queueWith(returnToken, five);
        SimpleStatementNode parse = new SimpleStatement().parse(queue);
        assertThat("Return expression is present", parse.getReturnNode().isPresent());
        assertThat("Return expression contains 5",
                parse.getReturnNode().get().getExpression().getLeft().getLeft().getLeft().getLiteral().get().getInteger().get(),
                is(equalTo(5)));
    }

    @Test
    public void readStatement() {
        ParseQueue queue = queueWith(read, op, id, cp);
        SimpleStatementNode parse = new SimpleStatement().parse(queue);
        assertThat("Read expression is present", parse.getReadNode().isPresent());
        assertThat("Read expression contains foobar",
                parse.getReadNode().get().getVariables().stream()
                        .map(variableNode -> variableNode.getIdentifier().getValue())
                        .collect(Collectors.toList()),
                is(equalTo(Collections.singletonList("foobar"))));
    }

    @Test
    public void readStatementWithMultipleArguments() {
        ParseQueue queue = queueWith(read, op, id, comma, id, cp);
        SimpleStatementNode parse = new SimpleStatement().parse(queue);
        assertThat("Read expression is present", parse.getReadNode().isPresent());
        assertThat("Read expression contains foobar, foobar",
                parse.getReadNode().get().getVariables().stream()
                        .map(variableNode -> variableNode.getIdentifier().getValue())
                        .collect(Collectors.toList()),
                is(equalTo(Arrays.asList("foobar", "foobar"))));
    }

    @Test
    public void writeStatement() {
        ParseQueue queue = queueWith(write, op, five, cp);
        SimpleStatementNode parse = new SimpleStatement().parse(queue);
        assertThat("Write expression is present", parse.getWriteNode().isPresent());
        assertThat("Write expression contains 5",
                parse.getWriteNode().get().getArguments().getArguments().stream()
                        .map(argument -> argument.getLeft().getLeft().getLeft().getLiteral().get().getInteger().get())
                        .collect(Collectors.toList()),
                is(equalTo(Collections.singletonList(5))));
    }

    @Test
    public void writeStatementWithMultipleArguments() {
        ParseQueue queue = queueWith(write, op, five, comma, six, cp);
        SimpleStatementNode parse = new SimpleStatement().parse(queue);
        assertThat("Write expression is present", parse.getWriteNode().isPresent());
        assertThat("Write expression contains 5, 6",
                parse.getWriteNode().get().getArguments().getArguments().stream()
                        .map(argument -> argument.getLeft().getLeft().getLeft().getLiteral().get().getInteger().get())
                        .collect(Collectors.toList()),
                is(equalTo(Arrays.asList(5, 6))));
    }

    @Test
    public void assertStatement() {
        ParseQueue queue = queueWith(assertToken, op, five, cp);
        SimpleStatementNode parse = new SimpleStatement().parse(queue);
        assertThat("Assert expression is present", parse.getAssertNode().isPresent());
        assertThat("Assert expression contains 5",
                parse.getAssertNode().get().getBooleanExpr().getLeft().getLeft().getLeft().getLiteral().get().getInteger().get(),
                is(equalTo(5)));
    }
}
