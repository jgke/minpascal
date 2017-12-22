package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.Configuration;
import fi.jgke.minpascal.data.Token;
import org.junit.Test;

import java.util.List;

import static fi.jgke.minpascal.data.TokenType.INTEGER_LITERAL;
import static fi.jgke.minpascal.data.TokenType.REAL_LITERAL;

public class NumberTokenizerTest extends ParserTest {

    @Test
    public void parseInt() {
        testParse("123", INTEGER_LITERAL, 123L);
        testParse("0", INTEGER_LITERAL, 0L);
    }

    @Test
    public void parseReal() {
        testParse("1.", REAL_LITERAL, 1.0);
        testParse("0.0", REAL_LITERAL, 0.0);
        testParse("13.4", REAL_LITERAL, 13.4);
        testParse("13.4e1", REAL_LITERAL, 134.);
        testParse("13.4e2", REAL_LITERAL, 1340.);
        testParse("12.1e+0", REAL_LITERAL, 12.1);
        testParse("12.1e+1", REAL_LITERAL, 121.);
        testParse("12.1e-1", REAL_LITERAL, 1.21);
    }

    @Test
    public void parseInvalidReal() {
        parseThrows("Unexpected end of file", () -> testParse("0e", REAL_LITERAL, 0.0));
        parseThrows("Invalid character 'a', expected a number", () -> testParse("0.0ea", REAL_LITERAL, 0.0));
    }

    @Test
    public void parseStrictInvalidReal() {
        Configuration.STRICT_MODE = true;
        parseThrows("Unexpected end of file", () -> testParse("0.", REAL_LITERAL, 0.0));
        parseThrows("Invalid character 'a', expected a number", () -> testParse("0.a", REAL_LITERAL, 0.0));
        parseThrows("Invalid character 'a', expected a number", () -> testParse("0.0ea", REAL_LITERAL, 0.0));
    }

    @Test
    public void parseTwoNumbers() {
        List<Token> tokens = getTokens("123 234");
        assertTypeAndValue("123", tokens.get(0), INTEGER_LITERAL, 123L);
        assertTypeAndValue("234", tokens.get(1), INTEGER_LITERAL, 234L);
    }

    @Test
    public void parseFailureOnSecondNumber() {
        parseThrows("end of file near line 1, column 1", () -> getTokens("123 1.e"));
    }
}
