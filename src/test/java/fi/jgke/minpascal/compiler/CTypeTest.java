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
