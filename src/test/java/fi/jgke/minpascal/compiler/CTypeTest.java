package fi.jgke.minpascal.compiler;

import com.google.common.collect.Streams;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
}
