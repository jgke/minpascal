package fi.jgke.minpascal.astparser;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.EmptyNode;
import fi.jgke.minpascal.astparser.parsers.Epsilon;
import fi.jgke.minpascal.astparser.parsers.Parser;
import fi.jgke.minpascal.astparser.parsers.TerminalMatch;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ParserTest {
    @Before
    public void init() {
        AstParser.setRules(Collections.singletonMap("whitespace", new Epsilon()));
    }

    @Test
    public void testEpsilon() {
        assertThat(new Epsilon().getName(), is(equalTo("epsilon")));
        parserTest(new Epsilon(), "foo", "foo", eps -> {
            assertThat(eps.getName(), is(equalTo("_epsilon")));
            assertThat("Eps instanceof EmptyNode", eps instanceof EmptyNode);
        });
    }

    @Test
    public void testTerminal() {
        assertThat(new TerminalMatch("foo", "bar", false).getName(), is(equalTo("foo")));
        parserTest(new TerminalMatch("foo", "pattern", false), "patternotherfoobar", "otherfoobar",
                match -> {
                    assertThat(match.getContentString(), is(equalTo("pattern")));
                    assertThat(match.getName(), is(equalTo("foo")));
                });
        parserTest(new TerminalMatch("foo", "pattern", true), "patternotherfoobar", "otherfoobar",
                match -> {
                    assertThat(match.getContentString(), is(equalTo("pattern")));
                    assertThat(match.getName(), is(equalTo("foo")));
                });
    }

    @Test(expected = CompilerException.class)
    public void testTerminalNotMatch() {
        new TerminalMatch("a", "a", false).parse("b");
    }

    @Test(expected = CompilerException.class)
    public void testTerminalNotMatchRegex() {
        new TerminalMatch("a", "a", true).parse("b");
    }

    private void parserTest(Parser parser, String str, String expectedRight, Consumer<AstNode> consumer) {
        assertThat("Parser " + parser + " can parse " + str, parser.parses(str));
        Pair<AstNode, String> parse = parser.parse(str);
        assertThat(parse.getRight(), is(equalTo(expectedRight)));
        consumer.accept(parse.getLeft());
    }
}
