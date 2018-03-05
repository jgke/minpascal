package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.parser.nodes.*;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class MiscTest {
    @Test
    public void hashcodes() {
        assertThat("FactorNode can be hashed", new FactorNode(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(new SizeNode(Optional.empty()))
        ).hashCode(), is(notNullValue()));
        assertThat("SimpleStatementNode can be hashed", new SimpleStatementNode(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(new CallNode(
                        Token.token(TokenType.IDENTIFIER, "foo", new Position(0, 0)),
                        new ArgumentsNode(Collections.emptyList()))),
                Optional.empty()
        ).hashCode(), is(notNullValue()));
        assertThat("StructuredStatementNode can be hashed", new StructuredStatementNode(
                Optional.of(new BlockNode(Collections.emptyList())),
                Optional.empty(),
                Optional.empty()
        ).hashCode(), is(notNullValue()));
        assertThat("StructuredStatementNode can be tostringed", new StructuredStatementNode(
                Optional.of(new BlockNode(Collections.emptyList())),
                Optional.empty(),
                Optional.empty()
        ).toString(), is(notNullValue()));
    }
}
