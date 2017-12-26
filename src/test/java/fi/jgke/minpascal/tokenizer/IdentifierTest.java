package fi.jgke.minpascal.tokenizer;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static fi.jgke.minpascal.data.TokenType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

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

    @Test
    public void parseKeywordIgnoreCase() {
        testParse("OR", OR, null);
        testParse("or", OR, null);
        testParse("Or", OR, null);
        testParse("oR", OR, null);
    }

    @Test
    public void parseTwoKeywordsSeparatedByNewline() {
        assertThat("begin and while get parsed as separate tokens",
                getTokens("begin\nwhile").stream().map(Token::getType).collect(Collectors.toList()),
                is(equalTo(Arrays.asList(BEGIN, WHILE, EOF))));
    }
}
