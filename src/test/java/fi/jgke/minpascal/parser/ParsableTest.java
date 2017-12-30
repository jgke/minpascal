package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

public class ParsableTest {
    private Parsable parsable = spy(Parsable.class);
    Token t1 = new Token(TokenType.BEGIN, Optional.empty(), new Position(0, 0));
    Token t2 = new Token(TokenType.PROGRAM, Optional.empty(), new Position(0, 0));

    private ParseQueue queueWith(Token... tokens) {
        ParseQueue queue = new ParseQueue();
        queue.addAll(Arrays.asList(tokens));
        return queue;
    }

    @Test
    public void testMatches() {
        when(parsable.getParsables()).thenReturn(Arrays.asList(t1.getType(), t2.getType()));
        ParseQueue queue = queueWith(t1, t2);
        assertThat("Default implementation matches on queue", parsable.matches(queue));
        verify(parsable).getParsables();
    }

    @Test
    public void testNotMatches() {
        when(parsable.getParsables()).thenReturn(Collections.singletonList(t2.getType()));
        ParseQueue queue = queueWith(t1);
        assertThat("Default implementation does not match on queue", !parsable.matches(queue));
        verify(parsable).getParsables();
    }

    @Test
    public void getMatchableTokensReturnsListOfTokens() {
        when(parsable.getParsables()).thenReturn(Arrays.asList(t1.getType(), t2.getType()));
        assertThat("Default implementation returns token type list",
                parsable.getMatchableTokens(),
                is(equalTo(Arrays.asList(t1.getType(), t2.getType()))));
        verify(parsable).getParsables();
    }

    @Test
    public void parseParsesFirstMatching() {
        Parsable parseMe = mock(Parsable.class);
        ParseQueue queue = queueWith(t2);
        TreeNode expected = new TreeNode();

        when(parseMe.matches(queue)).thenReturn(true);
        when(parseMe.parse(queue)).thenReturn(expected);

        when(parsable.getParsables()).thenReturn(Arrays.asList(t1.getType(), parseMe));

        assertThat("Default implementation returns parseWithIdentifier output",
                parsable.parse(queue),
                is(equalTo(expected)));

        verify(parseMe).matches(queue);
        verify(parseMe).parse(queue);
        verify(parsable).getParsables();
    }
}
