package fi.jgke.minpascal.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RegexTest {
    @Test
    public void constTest() {
        test("foo", "foobar", 3);
        test("fo+", "fooobar", 4);
        test("foo", "afoo", -1);
        test("", "afoo", 0);
        test("\\(foo\\)", "(foo)", 5);
        test("a.a", "aba", 3);
        test("a$", "ab", -1);
        test("a$", "a", 1);
        test("a*", "", 0);
        test("a*", "aaa", 3);
        test("ba*", "baa", 3);
        test("(foo)*", "", 0);
        test("(foo)*", "foo", 3);
        test("(foo)*", "foofoo", 6);
        test("(foo)*?fab", "foofab", 6);
        test("foo|bar|baz", "foo", 3);
        test("foo|bar|baz", "bar", 3);
        test("foo|bar|baz", "baz", 3);
        test("[a-c]+", "abac", 4);
        test("[^a-c]+", "abac", -1);
        test("[^a-c]+", "foo", 3);
        test("(foo)(bar)", "foobar", 6);
        test("[a-zA-Z_-]+", "FooBar_Baz-qux", 14);
        test("a|b|c", "a", 1);
        test("a|b|c", "b", 1);
        test("a|b|c", "c", 1);
        test(".", "", -1);
        test("(foo)*?bar", "fob", 0);
        test("[a-c]", "d", -1);
    }

    private void test(String pattern, String str, int expected) {
        int match = new Regex(pattern).match(str);
        assertThat("Expected '" + pattern + "' to match " +
                expected + " characters from '" + str + "'", match,
                is(equalTo(expected)));
    }
}
