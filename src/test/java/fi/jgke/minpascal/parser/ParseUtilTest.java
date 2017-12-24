package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseUtils;
import org.junit.Test;

import java.util.Arrays;

import static fi.jgke.minpascal.data.TokenType.*;
import static fi.jgke.minpascal.parser.base.ParseUtils.collectTokenTypes;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParseUtilTest {
    @Test
    public void constructorDoesntCrash() {
        new ParseUtils();
    }

    @Test
    public void collectTokenTypesTest() {
        Parsable parsable1 = mock(Parsable.class);
        Parsable parsable2 = mock(Parsable.class);
        when(parsable1.getMatchableTokens()).thenReturn(Arrays.asList(REAL_LITERAL, STRING_LITERAL));
        when(parsable2.getMatchableTokens()).thenReturn(Arrays.asList(STRING_LITERAL, IDENTIFIER));

        assertThat("collectTokenTypes() without arguments returns empty list",
                collectTokenTypes(), is(empty()));
        assertThat("collectTokenTypes() with single argument returns tokens as is",
                collectTokenTypes(parsable1), is(equalTo(Arrays.asList(REAL_LITERAL, STRING_LITERAL))));
        assertThat("collectTokenTypes() with multiple arguments returns list of unique tokens in order",
                collectTokenTypes(parsable1, parsable2),
                is(equalTo(Arrays.asList(REAL_LITERAL, STRING_LITERAL, IDENTIFIER))));
    }
}
