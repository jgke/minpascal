package fi.jgke.minpascal.astparser;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.EmptyNode;
import fi.jgke.minpascal.astparser.parsers.*;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static fi.jgke.minpascal.astparser.ParserTestUtils.initRules;
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

    @Test
    public void testRuleMatch() {
        initRules("a ::= 'b'");
        parserTest(new RuleMatch("a"), "b", "", a -> {
            assertThat(a.getName(), is(equalTo("a")));
            assertThat(a.getContentString(), is(equalTo("b")));
        });
        initRules("a ::= 'b'",
                "A ::= a");
        assertThat(new RuleMatch("A").getName(), is(equalTo("A")));
        parserTest(new RuleMatch("A"), "b", "", a -> {
            assertThat(a.getName(), is(equalTo("A")));
            assertThat(a.getFirstChild("a").getContentString(), is(equalTo("b")));
        });
    }

    @Test
    public void whitespaceSpecialCases() {
        initRules();
        // "whitespace" isn't run here
        parserTest(new TerminalMatch("whitespace", " ", false),
                "  ", " ",
                m -> assertThat(m.getContentString(), is(equalTo(" "))));
        //...but is here, on both sides
        parserTest(new TerminalMatch("a", "a", false),
                " a ", "",
                m -> assertThat(m.getContentString(), is(equalTo("a"))));
    }

    @Test
    public void testAndMatch() {
        initRules("a ::= 'c'",
                "b ::= 'd'");
        parserTest(new AndMatch("E", Arrays.asList(new RuleMatch("a"), new RuleMatch("b"))),
                "cdef", "ef", parse -> {
                    assertThat(parse.getName(), is(equalTo("E")));
                    assertThat(parse.getFirstChild("a").getContentString(), is(equalTo("c")));
                    assertThat(parse.getFirstChild("b").getContentString(), is(equalTo("d")));
                }
        );
        assertThat(new AndMatch("A", Collections.singletonList(new Epsilon())).getName(), is(equalTo("A")));
    }

    @Test
    public void testMaybeMatch() {
        initRules("a ::= 'a'",
                "b ::= 'b'",
                "A ::= a",
                "B ::= b");
        parserTest(new MaybeMatch("C", new RuleMatch("A"), new RuleMatch("B")),
                "ab", "", match -> {
                    assertThat("Left match is present", match.getFirstChild("A").toOptional().isPresent());
                    assertThat(match.getFirstChild("A").getFirstChild("a").getContentString(), is(equalTo("a")));
                    assertThat(match.getFirstChild("B").getFirstChild("b").getContentString(), is(equalTo("b")));
                });
        parserTest(new MaybeMatch("C", new RuleMatch("A"), new RuleMatch("B")),
                "b", "", match -> {
                    assertThat("Left match is empty", !match.getFirstChild("A").toOptional().isPresent());
                    assertThat(match.getFirstChild("B").getFirstChild("b").getContentString(), is(equalTo("b")));
                });
        parserTest(new MaybeMatch("C", new RuleMatch("A"), new Epsilon()),
                "b", "b", match -> assertThat("Left match is empty", !match.toOptional().isPresent()));
        parserTest(new MaybeMatch("C", new RuleMatch("A"), new Epsilon()),
                "a", "", match -> assertThat("Left match is present", match.toOptional().isPresent()));
        assertThat(new MaybeMatch("C", new RuleMatch("A"), new Epsilon()).getName(), is(equalTo("C")));
    }

    @Test
    public void testNotMatch() {
        initRules("a ::= 'a'",
                "b ::= 'b'",
                "A ::= a",
                "B ::= b");
        parserTest(new NotMatch(new RuleMatch("A"), new RuleMatch("B")),
                "b", "",
                m -> assertThat(m.getFirstChild("b").getContentString(), is(equalTo("b"))));
        assertThat("NotMatch doesn't match when it shouldn't",
                !new NotMatch(new RuleMatch("A"), new RuleMatch("A")).parses("a"));

        assertThat(new NotMatch(new RuleMatch("A"), new RuleMatch("B")).getName(), is(equalTo("B")));
    }

    @Test(expected = CompilerException.class)
    public void testNotMatchThrowsOnError() {
        initRules("a ::= 'a'");
        new NotMatch(new RuleMatch("a"), new RuleMatch("a")).parse("a");
    }

    @Test
    public void testOrMatch() {
        initRules("a ::= 'a'",
                "b ::= 'b'",
                "A ::= a",
                "B ::= b");
        parserTest(new OrMatch(Arrays.asList(new RuleMatch("A"), new RuleMatch("B"))),
                "a", "", m -> {
                    assertThat(m.getFirstChild("A").getFirstChild("a").getContentString(), is(equalTo("a")));
                    assertThat("A is present", m.getOptionalChild("A").isPresent());
                    assertThat("B is not present", !m.getOptionalChild("B").isPresent());
                });
        parserTest(new OrMatch(Arrays.asList(new RuleMatch("A"), new RuleMatch("B"))),
                "b", "", m -> {
                    assertThat(m.getFirstChild("B").getFirstChild("b").getContentString(), is(equalTo("b")));
                    assertThat("A is not present", !m.getOptionalChild("A").isPresent());
                    assertThat("B is present", m.getOptionalChild("B").isPresent());
                });
    }

    @Test
    public void testOrMatchFlattening() throws NoSuchFieldException, IllegalAccessException {
        Epsilon epsilon = new Epsilon();
        OrMatch orMatch = new OrMatch(Arrays.asList(epsilon, new OrMatch(Collections.emptyList())));
        Field f = orMatch.getClass().getDeclaredField("parsers");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Parser> parsers = (List<Parser>) f.get(orMatch);
        assertThat("Only one parser is present", parsers.size() == 1);
        assertThat(parsers.get(0), is(equalTo(epsilon)));
    }

    @Test(expected = CompilerException.class)
    public void testOrMatchThrowsOnError() {
        initRules("a ::= 'a'");
        new OrMatch(Arrays.asList(new RuleMatch("a"), new RuleMatch("a"))).parse("b");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOrMatchHasNoName() {
        new OrMatch(Collections.emptyList()).getName();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOrMatchWithWeirdParameters() {
        new OrMatch(Collections.singletonList(new OrMatch(Collections.emptyList())));
    }

    @Test
    public void testKleeneMatch() {
        initRules("a ::= 'a'",
                "b ::= 'b'",
                "A ::= a",
                "B ::= b");
        assertThat("KleeneMatch parses anything", new KleeneMatch(new RuleMatch("A")).parses("c"));
        assertThat("KleeneMatch parses anything", new KleeneMatch(new RuleMatch("A")).parses("a"));
        parserTest(new KleeneMatch(new RuleMatch("a")), "aaaa", "",
                m -> {
                    List<AstNode> list = m.getList();
                    assertThat(list.size(), is(equalTo(4)));
                    list.forEach(child -> assertThat(child.getContentString(), is(equalTo("a"))));
                });
        assertThat(new KleeneMatch(new Epsilon()).getName(), is(equalTo("more")));
    }

    private void parserTest(Parser parser, String str, String expectedRight, Consumer<AstNode> consumer) {
        assertThat("Parser " + parser + " can parse " + str, parser.parses(str));
        Pair<AstNode, Pair<String, Position>> parse = parser.parse(str);
        assertThat(parse.getRight().getLeft(), is(equalTo(expectedRight)));
        consumer.accept(parse.getLeft());
    }
}
