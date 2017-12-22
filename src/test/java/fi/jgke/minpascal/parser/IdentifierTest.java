package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.TokenType;
import org.junit.Test;

public class IdentifierTest extends ParserTest {
    @Test
    public void parseIdentifier() {
        testParse("foo", TokenType.IDENTIFIER, "foo");
        testParse("a_123", TokenType.IDENTIFIER, "a_123");
    }
}
