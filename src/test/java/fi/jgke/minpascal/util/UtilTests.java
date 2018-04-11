package fi.jgke.minpascal.util;

import fi.jgke.minpascal.exception.CompilerException;
import org.junit.Test;

import java.util.Optional;

import static fi.jgke.minpascal.util.OptionalUtils.assertOne;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class UtilTests {
    @Test
    public void untilTests() {
        new OptionalUtils();
        OptionalUtils.Until<String> stringUntil = new OptionalUtils.Until<>();
        stringUntil.chain(Optional.empty(), this::unreachable);
        stringUntil.chain(Optional.of("foo"), $ -> "bar");
        stringUntil.chain(Optional.empty(), this::unreachable);
        assertThat(stringUntil.get(), is(equalTo("bar")));
    }

    @Test
    public void assertOneTest() {
        assertOne(Optional.empty(), Optional.empty(), Optional.of(4));
    }

    @Test(expected = CompilerException.class)
    public void assertOneEmpty() {
        assertOne(Optional.empty(), Optional.empty());
    }

    @Test(expected = CompilerException.class)
    public void assertOneMultiple() {
        assertOne(Optional.of(5), Optional.of(10));
    }

    @Test
    public void formatterTest() {
        new Formatter();
        String input = "[foo{bar}, baz(quz; baq;)\n]";
        assertThat(Formatter.formatTree(input), is(equalTo("[\n  foo{bar},\n  baz(\n    quz;\n    baq;\n    \n  )\n  \n]\n")));
    }

    @SuppressWarnings("unused")
    private <T, U> T unreachable(U u) {
        throw new RuntimeException();
    }

}
