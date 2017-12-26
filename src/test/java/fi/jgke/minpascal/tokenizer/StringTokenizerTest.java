package fi.jgke.minpascal.tokenizer;

import fi.jgke.minpascal.Configuration;
import fi.jgke.minpascal.data.TokenType;
import org.junit.Test;

public class StringTokenizerTest extends TokenizerTest {
    @Test
    public void testString() {
        testParse("\"foo\"", TokenType.STRING_LITERAL, "foo");
        testParse("\"bar\\n\"", TokenType.STRING_LITERAL, "bar\n");
        testParse("\"\\\"foo\"", TokenType.STRING_LITERAL, "\"foo");
        testParse("\"foo\\t\"", TokenType.STRING_LITERAL, "foo\t");
        testParse("\"foo\\r\"", TokenType.STRING_LITERAL, "foo\r");
        testParse("\"fo\\o\"", TokenType.STRING_LITERAL, "foo");
        testParse("\"\"", TokenType.STRING_LITERAL, "");
        testParse("\"\uD83D\uDC31\"", TokenType.STRING_LITERAL, "\uD83D\uDC31");

        // Test using "'" as limiter
        testParse("'foo'", TokenType.STRING_LITERAL, "foo");
        Configuration.STRICT_MODE = true;
        parseThrows("Unexpected character '''", () -> getTokens("'foo'"));
    }

    @Test
    public void testFaultyStrings() {
        parseThrows("Unexpected end of file", () -> getTokens("\"bar"));
    }
}
