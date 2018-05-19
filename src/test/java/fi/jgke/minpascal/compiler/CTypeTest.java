package fi.jgke.minpascal.compiler;

import com.google.common.collect.Streams;
import fi.jgke.minpascal.astparser.AstParser;
import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.parsers.RuleParser;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.jgke.minpascal.compiler.CType.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CTypeTest {

    @Test
    public void toStringSimple() {
        testCType(
                Optional.empty(),
                Collections.emptyList(),
                "void",
                "void id");
        testCType(
                Optional.of(new CType("int")),
                Collections.singletonList(new CType("int")),
                "int (*)(int)",
                "int (*id)(int)",
                "int id(int a0)");
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

    private static void testCType(Optional<CType> call, List<CType> siblings, String expected, String expectedIdentifier) {
        testCType(call, siblings, expected, expectedIdentifier, "");
    }

    private static void testCType(Optional<CType> call, List<CType> siblings, String expected,
                                  String expectedIdentifier, String expectedFunction) {
        CType cType = new CType("void", call, siblings);
        assertThat(cType.formatType(), is(equalTo(expected)));
        assertThat(cType.toDeclaration("id", Optional.empty()), is(equalTo(expectedIdentifier)));
        if (!expectedFunction.isEmpty()) {
            assertThat(cType.toFunctionDeclaration(
                    Streams.mapWithIndex(siblings.stream(), ($, index) -> "a" + index).collect(Collectors.toList()),
                    "id"), is(equalTo(expectedFunction)));
        }
    }

    @Test(expected = AssertionError.class)
    public void testFpError1() {
        CType type;

        type = new CType("int");
        type.toFunctionDeclaration(Collections.emptyList(), "foo");
    }

    @Test(expected = AssertionError.class)
    public void testFpError2() {
        CType type;

        type = new CType(CINTEGER, Collections.emptyList());
        type.toFunctionDeclaration(Collections.singletonList("foo"), "foo");
    }

    @Test
    public void testAssignable() {
        CType type;

        type = CINTEGER;
        assertThat("int can be assigned to int", type.isAssignable(CINTEGER));
        assertThat("int can be assigned to int *", type.isAssignable(CType.ptrTo(CINTEGER)));
        assertThat("int can be assigned to double", type.isAssignable(CDOUBLE));
        assertThat("int can't be assigned to boolean", !type.isAssignable(CBOOLEAN));
        assertThat("double can't be assigned to int", !CDOUBLE.isAssignable(CINTEGER));
    }

    private void testFromTypeNode(String content, boolean ptr, CType expected) {
        AstNode typeNode = new RuleParser("Type").parse(content).getLeft();
        assertThat(CType.fromTypeNode(typeNode, ptr), is(equalTo(expected)));
    }

    @Test
    public void testFromTypeNode() {
        AstParser.initDefaultParsers();

        testFromTypeNode("real", false, CDOUBLE);
        testFromTypeNode("integer", false, CINTEGER);
        testFromTypeNode("string", false, CSTRING);
        testFromTypeNode("boolean", false, CBOOLEAN);
        testFromTypeNode("boolean", true, CType.ptrTo(CBOOLEAN));
        testFromTypeNode("array [3] of boolean", false, CType.ptrTo(CBOOLEAN));

    }
}
