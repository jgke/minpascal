package fi.jgke.minpascal.data;

import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.TerminalNode;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.jgke.minpascal.data.Token.token;
import static fi.jgke.minpascal.data.TokenType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class TokenTypeTest {
    private static final TokenType[] reservedIdentifiers = new TokenType[]{
            BOOLEAN, FALSE, INTEGER, READ, REAL, SIZE, STRING, TRUE, WRITELN
    };

    @Test
    public void testMatches() {
        Set<TokenType> reservedIdentifiers = Arrays
                .stream(TokenTypeTest.reservedIdentifiers)
                .collect(Collectors.toSet());

        for (TokenType t : TokenType.values()) {
            if (t.acceptedType().equals(Void.class)) {
                ParseQueue queue = new ParseQueue();
                queue.add(token(t, new Position(0, 0)));
                assertThat("Token matches on self", t.matches(queue));
            }
        }

        for (TokenType t : reservedIdentifiers) {
            ParseQueue queue = new ParseQueue();
            String parseText = t.toString().toLowerCase();
            queue.add(token(IDENTIFIER, parseText, new Position(0, 0)));
            queue.add(token(IDENTIFIER, "foo", new Position(0, 0)));
            assertThat("Reserved token matches on identifier with self text", t.matches(queue));
            queue.getIdentifier();
            assertThat("Reserved token does not match on identifier with other text", !t.matches(queue));
        }
    }

    @Test
    public void testMatchableTokens() {
        assertThat("PROGRAM matches on PROGRAM",
                PROGRAM.getMatchableTokens(),
                is(equalTo(Collections.singletonList(PROGRAM))));
        assertThat("BOOLEAN matches on IDENTIFIER",
                BOOLEAN.getMatchableTokens(),
                is(equalTo(Collections.singletonList(IDENTIFIER))));
    }

    @Test(expected = CompilerException.class)
    public void testSanityChecks1() {
        ParseQueue queue = new ParseQueue();
        queue.add(token(IDENTIFIER, new Position(-1, 0)));
        BOOLEAN.matches(queue);
    }

    @Test(expected = CompilerException.class)
    public void testSanityChecks2() {
        BOOLEAN.getParsables();
    }

    @Test
    public void testParse() {
        ParseQueue queue = new ParseQueue();
        Token<String> t1 = token(IDENTIFIER, "foo", new Position(0, 0));
        Token<String> t2 = token(IDENTIFIER, "Boolean", new Position(0, 0));
        Token<Void> t3 = token(WHILE, new Position(0, 0));
        queue.add(t1);
        queue.add(t2);
        queue.add(t3);
        assertThat("IDENTIFIER parses token", IDENTIFIER.parse(queue), is(equalTo(new TerminalNode(t1))));
        assertThat("BOOLEAN parses token", BOOLEAN.parse(queue), is(equalTo(new TerminalNode(t2))));
        assertThat("WHILE parses token", WHILE.parse(queue), is(equalTo(new TerminalNode(t3))));
    }
}
