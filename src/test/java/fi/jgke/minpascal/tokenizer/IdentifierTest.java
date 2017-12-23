package fi.jgke.minpascal.tokenizer;

import fi.jgke.minpascal.data.TokenType;
import org.junit.Test;

import java.util.Arrays;

import static fi.jgke.minpascal.data.TokenType.*;

public class IdentifierTest extends TokenizerTest {
    @Test
    public void parseIdentifier() {
        testParse("foo", IDENTIFIER, "foo");
        testParse("a_123", IDENTIFIER, "a_123");
    }

    private void parseKeyword(TokenType kw) {
        testParse(kw.toString().toLowerCase(), kw, null);
    }

    @Test
    public void parseKeyword() {
        TokenType[] keywords = new TokenType[]{
                OR, AND, NOT,
                IF, THEN, ELSE, OF, WHILE, DO, BEGIN, END,
                VAR, ARRAY, PROCEDURE, FUNCTION, PROGRAM, ASSERT,
        };

        Arrays.stream(keywords).forEach(this::parseKeyword);
    }
}
