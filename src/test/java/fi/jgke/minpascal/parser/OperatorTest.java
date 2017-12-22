package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.TokenType;
import org.junit.Test;

import static fi.jgke.minpascal.data.TokenType.*;

public class OperatorTest extends ParserTest {
    private void parseOperator(TokenType kw, String c) {
        testParse(c, kw, null);
    }

    @Test
    public void parseSimpleOperator() {
        parseOperator(PLUS, "+");
        parseOperator(MINUS, "-");
        parseOperator(TIMES, "*");
        parseOperator(MOD, "%");
        parseOperator(EQUALS, "=");
        parseOperator(OPENPAREN, "(");
        parseOperator(CLOSEPAREN, ")");
        parseOperator(OPENBRACKET, "[");
        parseOperator(CLOSEBRACKET, "]");
        parseOperator(DOT, ".");
        parseOperator(COMMA, ",");
        parseOperator(SEMICOLON, ";");
    }

    @Test
    public void parseComplexOperator() {
        parseOperator(LESSTHAN, "<");
        parseOperator(NOTEQUALS, "<>");
        parseOperator(LESSTHANEQUALS, "<=");

        parseOperator(MORETHAN, ">");
        parseOperator(MORETHANEQUALS, ">=");

        parseOperator(COLON, ":");
        parseOperator(ASSIGN, ":=");
    }

    @Test
    public void parseComplexOperatorWithOtherCharacter() {
        assertTypeAndValue("<a", getTokens("<a").get(0), LESSTHAN, null);
        assertTypeAndValue(">a", getTokens(">a").get(0), MORETHAN, null);
        assertTypeAndValue(":a", getTokens(":a").get(0), COLON, null);
    }
}
