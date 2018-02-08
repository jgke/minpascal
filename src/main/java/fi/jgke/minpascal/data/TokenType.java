package fi.jgke.minpascal.data;

import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.nodes.TerminalNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum TokenType implements Parsable {
    IDENTIFIER,
    INTEGER_LITERAL,
    REAL_LITERAL,
    STRING_LITERAL,

    PLUS, MINUS, TIMES, DIVIDE, MOD, EQUALS, NOTEQUALS, LESSTHAN, MORETHAN, LESSTHANEQUALS, MORETHANEQUALS,

    OPENPAREN, CLOSEPAREN, OPENBRACKET, CLOSEBRACKET, ASSIGN, DOT, COMMA, SEMICOLON, COLON,

    OR, AND, NOT,

    IF, THEN, ELSE, OF, WHILE, DO, BEGIN, END,

    VAR, ARRAY, PROCEDURE, FUNCTION, PROGRAM, ASSERT, RETURN,

    EOF,

    /* Reseved identifiers, these get parsed as IDENTIFIER */
    BOOLEAN, FALSE, INTEGER, READ, REAL, SIZE, STRING, TRUE, WRITELN;

    private static final TokenType[] resevedIdentifiers = new TokenType[]{
            BOOLEAN, FALSE, INTEGER, READ, REAL, SIZE, STRING, TRUE, WRITELN
    };

    @Override
    public boolean matches(ParseQueue queue) {
        return matches(queue.peek());
    }

    public boolean matches(Token<?> next) {
        if (next == null) {
            return false;
        }
        if (next.getType().equals(IDENTIFIER) && Arrays.asList(resevedIdentifiers).contains(this)) {
            //noinspection unchecked
            Token<String> token = (Token<String>) next;
            String value = token.getValue().toLowerCase();
            String expectedValue = this.toString().toLowerCase();
            return value.equals(expectedValue);
        }
        return next.getType().equals(this);
    }

    @Override
    public List<TokenType> getMatchableTokens() {
        if (Arrays.asList(resevedIdentifiers).contains(this)) {
            return Collections.singletonList(IDENTIFIER);
        }
        return Collections.singletonList(this);
    }

    @Override
    public List<Parsable> getParsables() {
        throw new CompilerException("getParsables() called on TokenType");
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        if (Arrays.asList(resevedIdentifiers).contains(this)) {
            return new TerminalNode(queue.getIdentifier());
        }
        return new TerminalNode(queue.getExpectedToken(this));
    }

    public Class<?> acceptedType() {
        if (Arrays.asList(resevedIdentifiers).contains(this)) {
            return String.class;
        }
        switch (this) {
            case IDENTIFIER:
                return String.class;
            case INTEGER_LITERAL:
                return Integer.class;
            case REAL_LITERAL:
                return Double.class;
            case STRING_LITERAL:
                return String.class;
            default:
                return Void.class;
        }
    }
}
