package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.TokenType;
import org.junit.Test;

public class StringTokenizerTest extends ParserTest {
    @Test
    public void testString() {
        testParse("\"foo\"", TokenType.STRING_LITERAL, "foo");
        testParse("\"bar\\n\"", TokenType.STRING_LITERAL, "bar\n");
        testParse("\"\\\"foo\"", TokenType.STRING_LITERAL, "\"foo");
        testParse("\"foo\\t\"", TokenType.STRING_LITERAL, "foo\t");
        testParse("\"foo\\r\"", TokenType.STRING_LITERAL, "foo\r");
        testParse("\"fo\\o\"", TokenType.STRING_LITERAL, "foo");
        testParse("\"\uD83D\uDC31\"", TokenType.STRING_LITERAL, "\uD83D\uDC31");
    }

    @Test
    public void testFaultyStrings() {
        parseThrows("Unexpected end of file", () -> getTokens("\"bar"));
        parseThrows("Expected a delimiter", () -> getTokens("\"bar\"foo"));
    }
}
