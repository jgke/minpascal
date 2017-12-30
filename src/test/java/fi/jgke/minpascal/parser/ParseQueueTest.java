package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.exception.UnexpectedTokenException;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static fi.jgke.minpascal.TestUtils.queueWith;
import static fi.jgke.minpascal.data.TokenType.COMMA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

public class ParseQueueTest {

    Token t1 = new Token(TokenType.BEGIN, Optional.empty(), new Position(0, 0));
    Token t2 = new Token(TokenType.PROGRAM, Optional.empty(), new Position(0, 0));
    Token t3 = new Token(TokenType.MINUS, Optional.empty(), new Position(0, 0));
    Token comma = new Token(COMMA, Optional.empty(), new Position(0, 0));
    Token closeParen = new Token(TokenType.CLOSEPAREN, Optional.empty(), new Position(0, 0));

    @Test
    public void getExpectedTokenReturnsExpectedToken() {
        assertThat("getExpectedToken returns expected token with single argument",
                queueWith(t1, t2).getExpectedToken(t1.getType()), is(equalTo(t1)));
        assertThat("getExpectedToken returns expected token with multiple arguments",
                queueWith(t1, t2).getExpectedToken(t1.getType(), t2.getType()), is(equalTo(t1)));
    }

    @Test(expected = UnexpectedTokenException.class)
    public void getExpectedTokenThrows() {
        queueWith(t1, t2).getExpectedToken(t3.getType());
    }

    @Test
    public void getExpectedTokensReturnsExpectedToken() {
        ParseQueue queue = queueWith(t1, t2, t3);
        queue.getExpectedTokens(t1.getType(), t2.getType());
        assertThat("getExpectedToken drops expected tokens",
                queue.getExpectedToken(t3.getType()), is(equalTo(t3)));
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void getExpectedTokensThrows() {
        thrown.expect(UnexpectedTokenException.class);
        thrown.expectMessage(Matchers.containsString("expected " + t2.getType() + " near"));
        ParseQueue queue = queueWith(t1, t3);
        queue.getExpectedTokens(t1.getType(), t2.getType());
    }

    @Test
    public void getExpectedTokenThrowsWithSeveralExpected() {
        thrown.expect(UnexpectedTokenException.class);
        thrown.expectMessage(Matchers.containsString("expected any of"));
        ParseQueue queue = queueWith(t1);
        queue.getExpectedToken(t2.getType(), t3.getType());
    }

    @Test
    public void isNextReturnsCorrectValues() {
        ParseQueue queue = queueWith(t1, t2, t3);
        assertThat("t1 is the next token", queue.isNext(t1.getType()), is(true));
        assertThat("t2 not is the next token", queue.isNext(t2.getType()), is(false));
    }

    @Test
    public void anyMatchesReturnsCorrectValues() {
        ParseQueue queue = queueWith(t1, t2, t3);

        Parsable parsable1 = mock(Parsable.class);
        Parsable parsable2 = mock(Parsable.class);
        Parsable parsable3 = mock(Parsable.class);
        when(parsable1.matches(queue)).thenReturn(false);
        when(parsable2.matches(queue)).thenReturn(true);
        when(parsable3.matches(queue)).thenReturn(false);

        assertThat("anyMatches returns true when any of the parsers matches",
                queue.anyMatches(parsable1, parsable2, parsable3), is(true));
        assertThat("anyMatches returns false when none of the parsers matches",
                queue.anyMatches(parsable1, parsable3), is(false));
    }

    @Test
    public void anyCallsCorrectParseMethod() {
        ParseQueue queue = queueWith(t1, t2, t3);
        TreeNode expected = new TreeNode();

        Parsable parsable1 = mock(Parsable.class);
        Parsable parsable2 = mock(Parsable.class);
        Parsable parsable3 = mock(Parsable.class);
        when(parsable1.matches(queue)).thenReturn(false);
        when(parsable2.matches(queue)).thenReturn(true);
        when(parsable2.parse(queue)).thenReturn(expected);
        when(parsable3.matches(queue)).thenReturn(true);

        assertThat("any calls the first parseWithIdentifier method which matches",
                queue.any(parsable1, parsable2, parsable3), is(equalTo(expected)));

        verify(parsable1).matches(queue);
        verify(parsable2).matches(queue);
        verify(parsable2).parse(queue);
        verifyNoMoreInteractions(parsable1, parsable2, parsable3);
    }

    @Test
    public void anyThrowsWhenNoneMatch() {
        ParseQueue queue = queueWith(t1, t2, t3);
        thrown.expect(UnexpectedTokenException.class);
        thrown.expectMessage(Matchers.containsString("expected " + t3.getType() + " near"));

        Parsable parsable1 = mock(Parsable.class);
        Parsable parsable2 = mock(Parsable.class);
        Parsable parsable3 = mock(Parsable.class);
        when(parsable1.matches(queue)).thenReturn(false);
        when(parsable1.getMatchableTokens()).thenReturn(Collections.singletonList(t3.getType()));

        queue.any(parsable1, parsable2, parsable3);
    }

    @Test
    public void anyThrowsWhenNoneMatchWithMultipleOptions() {
        ParseQueue queue = queueWith(t1, t2, t3);
        thrown.expect(UnexpectedTokenException.class);
        thrown.expectMessage(Matchers.containsString("expected any of"));

        Parsable parsable1 = mock(Parsable.class);
        Parsable parsable2 = mock(Parsable.class);
        Parsable parsable3 = mock(Parsable.class);
        when(parsable1.matches(queue)).thenReturn(false);
        when(parsable2.matches(queue)).thenReturn(false);
        when(parsable3.matches(queue)).thenReturn(false);

        queue.any(parsable1, parsable2, parsable3);
    }

    @Test
    public void ifNextConsumeConsumes() {
        ParseQueue queue = queueWith(t1, t2, t3);

        assertThat("ifNextConsume returns true on consume", queue.ifNextConsume(t1.getType()));
        assertThat("ifNextConsume removed the token", queue.isNext(t2.getType()));
    }

    @Test
    public void ifNextConsumeDoesNotConsume() {
        ParseQueue queue = queueWith(t1, t2, t3);

        assertThat("ifNextConsume returns false when not consumed", !queue.ifNextConsume(t2.getType()));
        assertThat("ifNextConsume did not remove a token", queue.isNext(t1.getType()));
    }

    @Test
    public void collectByCommaCollectsTwoItems() {
        ParseQueue queue = queueWith(t1, comma, t1, closeParen, t3);
        assertThat("collectBy returns t1 and t2",
                queue.collectBy(t -> queue.getExpectedToken(t1.getType()), COMMA),
                is(Arrays.asList(t1, t1)));
    }

    @Test
    public void collectByCommaCollectsOneItem() {
        ParseQueue queue = queueWith(t1, closeParen, t3);
        assertThat("collectBy returns t1",
                queue.collectBy(t -> queue.getExpectedToken(t1.getType()), COMMA),
                is(Collections.singletonList(t1)));
    }

    @Test
    public void collectByCommaCollectsEmptyList() {
        ParseQueue queue = queueWith(closeParen, t3);
        assertThat("collectBy returns empty list",
                queue.collectBy(t -> queue.getExpectedToken(t1.getType()), COMMA),
                is(Collections.emptyList()));
    }

    @Test(expected = UnexpectedTokenException.class)
    public void collectByCommaThrowsWhenAskedTo() {
        ParseQueue queue = queueWith(closeParen, t3);
        queue.collectBy(t -> queue.getExpectedToken(t1.getType()), true, COMMA, q -> false, false);
    }
}
