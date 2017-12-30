package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.blocks.Statement;
import fi.jgke.minpascal.parser.nodes.StatementNode;
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
public class StatementTest {

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
    public void callStatementWithArgument() {
        ParseQueue queue = queueWith(id, op, five, cp);
        StatementNode parse = new Statement().parse(queue);
        assertThat("Call expression is present", parse.getSimple().get().getCallNode().isPresent());
        assertThat("Call identifier contains foobar",
                parse.getSimple().get().getCallNode().get().getIdentifier().getValue().get(),
                is(equalTo("foobar")));
        assertThat("Call arguments contain 5",
                parse.getSimple().get().getCallNode().get().getArguments().getArguments().stream()
                        .map(e -> e.getLeft().getLeft().getLeft().getLiteral().get().getInteger().get()).collect(Collectors.toList()),
                is(equalTo(Collections.singletonList(5))));
    }

    @Test
    public void callStatementWithMultipleArguments() {
        ParseQueue queue = queueWith(id, op, five, comma, six, comma, five, cp);
        StatementNode parse = new Statement().parse(queue);
        assertThat("Call expression is present", parse.getSimple().get().getCallNode().isPresent());
        assertThat("Call identifier contains foobar",
                parse.getSimple().get().getCallNode().get().getIdentifier().getValue().get(),
                is(equalTo("foobar")));
        assertThat("Call arguments contain 5, 6, 5",
                parse.getSimple().get().getCallNode().get().getArguments().getArguments().stream()
                        .map(e -> e.getLeft().getLeft().getLeft().getLiteral().get().getInteger().get()).collect(Collectors.toList()),
                is(equalTo(Arrays.asList(5, 6, 5))));
    }
}
