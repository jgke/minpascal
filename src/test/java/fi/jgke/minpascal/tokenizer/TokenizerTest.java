package fi.jgke.minpascal.tokenizer;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.exception.ParseException;
import fi.jgke.minpascal.util.Stub;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class TokenizerTest extends TestBase {
    protected List<Token> getTokens(String content) {
        List<Token> tokens = new Tokenizer(content).tokenize().collect(Collectors.toList());
        assertThat("Last token is EOF", tokens.get(tokens.size() - 1).getType(), is(equalTo(TokenType.EOF)));
        return tokens;
    }

    protected Token getSingleToken(String content) {
        List<Token> tokens = getTokens(content);
        assertThat("Only one real token was parsed", tokens.size(), is(equalTo(2)));
        return tokens.get(0);
    }

    protected void assertTypeAndValue(String content, Token token, TokenType expectedType, Object expectedValue) {
        assertThat("Tokenizer parses " + content + " as " + expectedType,
                token.getType(), is(equalTo(expectedType)));
        assertThat("Tokenizer parses " + content + " as " + expectedValue,
                token.getValue(), is(Optional.ofNullable(expectedValue)));
    }

    protected void testParse(String content, TokenType expectedType, Object expectedValue) {
        Token token = getSingleToken(content);
        assertTypeAndValue(content, token, expectedType, expectedValue);
    }

    protected void parseThrows(String partOfMessage, Stub stub) {
        try {
            stub.call();
            // This should be unreachable
            assertThat("Parsing throws exception", false, is(true));
        } catch (ParseException e) {
            if (!e.getMessage().contains(partOfMessage)) {
                // Rethrow so test shows the stack trace
                throw e;
            }
            assertThat("Parsing throws correct exception", e.getMessage(), containsString(partOfMessage));
            return;
        }
        assertThat("Call throws exception", false, is(true));
    }

    @Test
    public void parseEmpty() {
        assertThat("Parsing empty string is EOF", getTokens("").size(), is(equalTo(1)));
    }

    @Test
    public void parseUnknown() {
        parseThrows("Unexpected character", () -> getTokens("€"));
    }

    private boolean isLetter(char c) {
        return Tokenizer.isLetter(new Tokenizer.CharacterWithPosition(c, null));
    }

    private boolean isDigit(char c) {
        return Tokenizer.isDigit(new Tokenizer.CharacterWithPosition(c, null));
    }

    private boolean isIdentifierCharacter(char c) {
        return Tokenizer.isIdentifierCharacter(new Tokenizer.CharacterWithPosition(c, null));
    }

    @Test
    public void detectorFunctions() {
        assertThat("A is a letter", isLetter('A'), is(true));
        assertThat("J is a letter", isLetter('J'), is(true));
        assertThat("Z is a letter", isLetter('Z'), is(true));
        assertThat("a is a letter", isLetter('a'), is(true));
        assertThat("j is a letter", isLetter('j'), is(true));
        assertThat("z is a letter", isLetter('z'), is(true));

        assertThat("null is not a letter", Tokenizer.isLetter(null), is(false));
        assertThat("@ is not a letter", isLetter('@'), is(false));
        assertThat("[ is not a letter", isLetter('['), is(false));
        assertThat("` is not a letter", isLetter('`'), is(false));
        assertThat("{ is not a letter", isLetter('{'), is(false));

        assertThat("0 is a digit", isDigit('0'), is(true));
        assertThat("9 is a digit", isDigit('9'), is(true));

        assertThat("/ is not a digit", isDigit('/'), is(false));
        assertThat(": is not a digit", isDigit(':'), is(false));

        assertThat("null is not an identifier character", Tokenizer.isIdentifierCharacter(null), is(false));
        assertThat("% is not identifier character", isIdentifierCharacter('%'), is(false));
        assertThat("a is an identifier character", isIdentifierCharacter('a'), is(true));
        assertThat("1 is an identifier character", isIdentifierCharacter('1'), is(true));
        assertThat("_ is an identifier character", isIdentifierCharacter('_'), is(true));
    }

    @Test
    public void errorMessages() {
        parseThrows("Unexpected end of file near line 3, column 1", () -> getTokens("\n\n\"foo"));
        parseThrows("Unexpected end of file near line 2, column 5", () -> getTokens("\n123 1e"));
    }
}
