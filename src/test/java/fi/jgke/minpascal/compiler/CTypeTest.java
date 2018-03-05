package fi.jgke.minpascal.compiler;

import com.google.common.collect.Streams;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.nodes.*;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.jgke.minpascal.data.TokenType.IDENTIFIER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CTypeTest {

    @Test
    public void toStringSimple() {
        testCType(
                "void",
                Optional.empty(),
                Collections.emptyList(),
                "void",
                "void id");
        testCType(
                "anything",
                Optional.of(new CType("int")),
                Collections.singletonList(new CType("int")),
                "int (*)(int)",
                "int (*id)(int)",
                "int id(int a0)");
    }

    @Test
    public void testFromPascal() {
        CType type;

        type = new CType(new TypeNode(
                Optional.of(new SimpleTypeNode(Token.token(IDENTIFIER, "integer", new Position(0, 0)))),
                Optional.empty()));
        assertThat(type.toDeclaration("foo"), is(equalTo("int foo")));

        type = new CType(new TypeNode(
                Optional.of(new SimpleTypeNode(Token.token(IDENTIFIER, "boolean", new Position(0, 0)))),
                Optional.empty()));
        assertThat(type.toDeclaration("foo"), is(equalTo("bool foo")));

        type = new CType(new TypeNode(
                Optional.of(new SimpleTypeNode(Token.token(IDENTIFIER, "string", new Position(0, 0)))),
                Optional.empty()));
        assertThat(type.toDeclaration("foo"), is(equalTo("char * foo")));

        type = new CType(new TypeNode(
                Optional.empty(),
                Optional.of(new ArrayTypeNode(
                        createLiteral(new LiteralNode(
                                Optional.of(5),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()
                        )),
                        new SimpleTypeNode(Token.token(IDENTIFIER, "integer", new Position(0, 0)))))));
        assertThat(type.toDeclaration("foo"), is(equalTo("int * foo")));

    }

    private ExpressionNode createLiteral(LiteralNode literal) {
        return new ExpressionNode(new SimpleExpressionNode(Optional.empty(), new TermNode(new FactorNode(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(literal),
                Optional.empty()
        ))));
    }

    @Test(expected = CompilerException.class)
    public void testInvalidType() {
        CType type = new CType(new TypeNode(
                Optional.of(new SimpleTypeNode(Token.token(IDENTIFIER, "other", new Position(0, 0)))),
                Optional.empty()));
        assertThat(type.toDeclaration("foo"), is(equalTo("char * foo")));
    }

    @Test
    public void testFormat() {
        CType type;

        type = new CType("int");
        assertThat(type.toFormat(), is(equalTo("%d")));

        type = new CType("bool");
        assertThat(type.toFormat(), is(equalTo("%d")));

        type = new CType("char *");
        assertThat(type.toFormat(), is(equalTo("%s")));

        type = new CType("double");
        assertThat(type.toFormat(), is(equalTo("%f")));

        type = new CType("int *");
        assertThat(type.toFormat(), is(equalTo("%p")));

        type = new CType("void", Optional.of(new CType("int")), Collections.emptyList());
        assertThat(type.toFormat(), is(equalTo("%p")));

        type = new CType("void", Optional.empty(), Collections.singletonList(new CType("int")));
        assertThat(type.toFormat(), is(equalTo("%p")));

        type = new CType("void", Optional.of(new CType("int")), Collections.singletonList(new CType("int")));
        assertThat(type.toFormat(), is(equalTo("%p")));
    }

    @Test
    public void testDefaultValue() {
        CType type;

        type = new CType("int");
        assertThat(type.defaultValue(), is(equalTo("0")));

        type = new CType("bool");
        assertThat(type.defaultValue(), is(equalTo("0")));

        type = new CType("double");
        assertThat(type.defaultValue(), is(equalTo("0.0d")));

        type = new CType("char *");
        assertThat(type.defaultValue(), is(equalTo("NULL")));
    }

    private static void testCType(String me, Optional<CType> call, List<CType> siblings, String expected, String expectedIdentifier) {
        testCType(me, call, siblings, expected, expectedIdentifier, "");
    }

    private static void testCType(String me, Optional<CType> call, List<CType> siblings, String expected,
                                  String expectedIdentifier, String expectedFunction) {
        CType cType = new CType(me, call, siblings);
        assertThat(cType.toString(), is(equalTo(expected)));
        assertThat(cType.toDeclaration("id"), is(equalTo(expectedIdentifier)));
        if (!expectedFunction.isEmpty()) {
            assertThat(cType.toFunctionDeclaration(
                    Streams.mapWithIndex(siblings.stream(), ($, index) -> "a" + index).collect(Collectors.toList()),
                    "id"), is(equalTo(expectedFunction)));
        }
    }
}
