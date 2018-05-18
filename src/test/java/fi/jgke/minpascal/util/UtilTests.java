package fi.jgke.minpascal.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class UtilTests {
    @Test
    public void formatterTest() {
        new Formatter();
        String input = "[foo{bar}, baz(quz; baq;)\n]";
        assertThat(Formatter.formatTree(input, -1), is(equalTo("\n[\n  foo{bar}\n  baz (\n    quz;\n    baq;)]\n")));
        assertThat(Formatter.formatTree(input, 0), is(equalTo("\n")));
        assertThat(Formatter.formatTree(input, 1), is(equalTo("\n[]\n")));
        assertThat(Formatter.formatTree(input, 2), is(equalTo("\n[\n  foo{bar}\n  baz ()]\n")));
        assertThat(Formatter.formatTree("[a[b[c],d],e[f]]", -1), is(equalTo("\n[\n  a[\n    b[\n      c]\n    d]\n  e[\n    f]]\n")));
    }

    @Test
    public void pairTest() {
        Pair<Integer, String> foo = new Pair<>(1, "foo");
        assertThat(foo.getLeft(), is(equalTo(1)));
        assertThat(foo.getRight(), is(equalTo("foo")));
    }

    @SuppressWarnings("unused")
    private <T, U> T unreachable(U u) {
        throw new RuntimeException();
    }

}
