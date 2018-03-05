package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.exception.ParseException;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.blocks.SimpleType;
import fi.jgke.minpascal.parser.blocks.Type;
import org.junit.Test;

import static fi.jgke.minpascal.TestUtils.queueWith;
import static fi.jgke.minpascal.data.TokenType.IDENTIFIER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class TypeParseTest {
    private final Token<String> integer = Token.token(IDENTIFIER, "integer", new Position(0, 0));
    private final Token<String> string = Token.token(IDENTIFIER, "string", new Position(0, 0));
    private final Token<String> bool = Token.token(IDENTIFIER, "boolean", new Position(0, 0));
    private final Token<String> real = Token.token(IDENTIFIER, "real", new Position(0, 0));
    private final Token<String> other = Token.token(IDENTIFIER, "other", new Position(0, 0));

    @Test
    public void testSimpleType() {
        assertThat("Type parses", new SimpleType().parse(queueWith(integer)).getType(), is(equalTo(integer)));
        assertThat("Type parses", new SimpleType().parse(queueWith(string)).getType(), is(equalTo(string)));
        assertThat("Type parses", new SimpleType().parse(queueWith(bool)).getType(), is(equalTo(bool)));
        assertThat("Type parses", new SimpleType().parse(queueWith(real)).getType(), is(equalTo(real)));
    }

    @Test(expected = ParseException.class)
    public void testOtherType() {
        new SimpleType().parse(queueWith(other));
    }

    @Test
    public void typeMatchesToIdentifier(){
        assertThat("Type matches", queueWith(real).anyMatches(new Type().getParsables().toArray(new Parsable[0])));
    }
}
