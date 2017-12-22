package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.Configuration;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.exception.ParseException;
import fi.jgke.minpascal.tokenizer.Tokenizer;
import fi.jgke.minpascal.util.Stub;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.jgke.minpascal.data.TokenType.INTEGER_LITERAL;
import static fi.jgke.minpascal.data.TokenType.REAL_LITERAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


public class NumberTokenizerTest {
    private Token getSingleToken(String content) {
        List<Token> tokens = new Tokenizer(content).tokenize().collect(Collectors.toList());
        assertThat("Only one real token was parsed", tokens.size(), is(equalTo(2)));
        assertThat("Second token is EOF", tokens.get(1).getType(), is(equalTo(TokenType.EOF)));
        return tokens.get(0);
    }

    private void testParse(String content, TokenType expectedType, Object expectedValue) {
        Token token = getSingleToken(content);
        assertThat("Tokenizer parses " + content + " as " + expectedType,
                token.getType(), is(equalTo(expectedType)));
        if (token.getValue().isPresent()) {
            assertThat("Tokenizer parses " + content + " as " + expectedValue,
                    token.getValue().get(), is(equalTo(expectedValue)));
        } else {
            // Welp, fail the test
            assertThat("Token value is present",
                    token.getValue().isPresent(), is(true));
        }
    }

    @Test
    public void parseInt() {
        testParse("123", INTEGER_LITERAL, 123L);
        testParse("0", INTEGER_LITERAL, 0L);
    }

    @Test
    public void parseReal() {
        testParse("0.0", REAL_LITERAL, 0.0);
        testParse("13.4", REAL_LITERAL, 13.4);
        testParse("13.4e1", REAL_LITERAL, 134.);
        testParse("13.4e2", REAL_LITERAL, 1340.);
        testParse("12.1e+0", REAL_LITERAL, 12.1);
        testParse("12.1e-1", REAL_LITERAL, 1.21);
    }

    public void parseThrows(String partOfMessage, Stub stub) {
        try {
            stub.call();
            // This should be unreachable
            assertThat("Parsing throws exception", true, is(false));
        } catch (ParseException e) {
            if (!e.getMessage().contains(partOfMessage)) {
                // Rethrow so test shows the stack trace
                throw e;
            }
            assertThat("Parsing throws correct exception", e.getMessage(), containsString(partOfMessage));
        }
    }

    @Test
    public void parseInvalidReal() {
        parseThrows("Unexpected end of file", () -> testParse("0e", REAL_LITERAL, 0.0));
        Configuration.STRICT_MODE = true;
        parseThrows("Unexpected end of file", () -> testParse("0.", REAL_LITERAL, 0.0));
        parseThrows("Invalid character 'a', expected a number", () -> testParse("0.a", REAL_LITERAL, 0.0));
    }
}
