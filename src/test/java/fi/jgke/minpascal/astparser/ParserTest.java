package fi.jgke.minpascal.astparser;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.EmptyNode;
import fi.jgke.minpascal.astparser.parsers.*;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.exception.ParseError;
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
        assertThat(new TerminalParser("foo", "bar", false).getName(), is(equalTo("foo")));
        parserTest(new TerminalParser("foo", "pattern", false), "patternotherfoobar", "otherfoobar",
                match -> {
                    assertThat(match.getContentString(), is(equalTo("pattern")));
                    assertThat(match.getName(), is(equalTo("foo")));
                });
        parserTest(new TerminalParser("foo", "pattern", true), "patternotherfoobar", "otherfoobar",
                match -> {
                    assertThat(match.getContentString(), is(equalTo("pattern")));
                    assertThat(match.getName(), is(equalTo("foo")));
                });
    }

    @Test(expected = ParseError.class)
    public void testTerminalNotMatch() {
        new TerminalParser("a", "a", false).parse("b");
    }

    @Test(expected = ParseError.class)
    public void testTerminalNotMatchRegex() {
        new TerminalParser("a", "a", true).parse("b");
    }

    @Test
    public void testRuleMatch() {
        initRules("a ::= 'b'");
        parserTest(new RuleParser("a"), "b", "", a -> {
            assertThat(a.getName(), is(equalTo("a")));
            assertThat(a.getContentString(), is(equalTo("b")));
        });
        initRules("a ::= 'b'",
                "A ::= a");
        assertThat(new RuleParser("A").getName(), is(equalTo("A")));
        parserTest(new RuleParser("A"), "b", "", a -> {
            assertThat(a.getName(), is(equalTo("A")));
            assertThat(a.getFirstChild("a").getContentString(), is(equalTo("b")));
        });
    }

    @Test
    public void whitespaceSpecialCases() {
        initRules();
        // "whitespace" isn't run here
        parserTest(new TerminalParser("whitespace", " ", false),
                "  ", " ",
                m -> assertThat(m.getContentString(), is(equalTo(" "))));
        //...but is here, on both sides
        parserTest(new TerminalParser("a", "a", false),
                " a ", "",
                m -> assertThat(m.getContentString(), is(equalTo("a"))));
    }

    @Test
    public void testAndMatch() {
        initRules("a ::= 'c'",
                "b ::= 'd'");
        parserTest(new AndParser("E", Arrays.asList(new RuleParser("a"), new RuleParser("b"))),
                "cdef", "ef", parse -> {
                    assertThat(parse.getName(), is(equalTo("E")));
                    assertThat(parse.getFirstChild("a").getContentString(), is(equalTo("c")));
                    assertThat(parse.getFirstChild("b").getContentString(), is(equalTo("d")));
                }
        );
        assertThat(new AndParser("A", Collections.singletonList(new Epsilon())).getName(), is(equalTo("A")));
    }

    @Test
    public void testMaybeMatch() {
        initRules("a ::= 'a'",
                "b ::= 'b'",
                "A ::= a",
                "B ::= b");
        parserTest(new MaybeParser("C", new RuleParser("A"), new RuleParser("B")),
                "ab", "", match -> {
                    assertThat("Left match is present", match.getFirstChild("A").toOptional().isPresent());
                    assertThat(match.getFirstChild("A").getFirstChild("a").getContentString(), is(equalTo("a")));
                    assertThat(match.getFirstChild("B").getFirstChild("b").getContentString(), is(equalTo("b")));
                });
        parserTest(new MaybeParser("C", new RuleParser("A"), new RuleParser("B")),
                "b", "", match -> {
                    assertThat("Left match is empty", !match.getFirstChild("A").toOptional().isPresent());
                    assertThat(match.getFirstChild("B").getFirstChild("b").getContentString(), is(equalTo("b")));
                });
        parserTest(new MaybeParser("C", new RuleParser("A"), new Epsilon()),
                "b", "b", match -> assertThat("Left match is empty", !match.toOptional().isPresent()));
        parserTest(new MaybeParser("C", new RuleParser("A"), new Epsilon()),
                "a", "", match -> assertThat("Left match is present", match.toOptional().isPresent()));
        assertThat(new MaybeParser("C", new RuleParser("A"), new Epsilon()).getName(), is(equalTo("C")));
    }

    @Test
    public void testNotMatch() {
        initRules("a ::= 'a'",
                "b ::= 'b'",
                "A ::= a",
                "B ::= b");
        parserTest(new NotParser(new RuleParser("A"), new RuleParser("B")),
                "b", "",
                m -> assertThat(m.getFirstChild("b").getContentString(), is(equalTo("b"))));
        assertThat("NotParser doesn't match when it shouldn't",
                !new NotParser(new RuleParser("A"), new RuleParser("A")).parses("a"));

        assertThat(new NotParser(new RuleParser("A"), new RuleParser("B")).getName(), is(equalTo("B")));
    }

    @Test(expected = CompilerException.class)
    public void testNotMatchThrowsOnError() {
        initRules("a ::= 'a'");
        new NotParser(new RuleParser("a"), new RuleParser("a")).parse("a");
    }

    @Test
    public void testOrMatch() {
        initRules("a ::= 'a'",
                "b ::= 'b'",
                "A ::= a",
                "B ::= b");
        parserTest(new OrParser(Arrays.asList(new RuleParser("A"), new RuleParser("B"))),
                "a", "", m -> {
                    assertThat(m.getFirstChild("A").getFirstChild("a").getContentString(), is(equalTo("a")));
                    assertThat("A is present", m.getOptionalChild("A").isPresent());
                    assertThat("B is not present", !m.getOptionalChild("B").isPresent());
                });
        parserTest(new OrParser(Arrays.asList(new RuleParser("A"), new RuleParser("B"))),
                "b", "", m -> {
                    assertThat(m.getFirstChild("B").getFirstChild("b").getContentString(), is(equalTo("b")));
                    assertThat("A is not present", !m.getOptionalChild("A").isPresent());
                    assertThat("B is present", m.getOptionalChild("B").isPresent());
                });
    }

    @Test
    public void testOrMatchFlattening() throws NoSuchFieldException, IllegalAccessException {
        Epsilon epsilon = new Epsilon();
        OrParser orParser = new OrParser(Arrays.asList(epsilon, new OrParser(Collections.emptyList())));
        Field f = orParser.getClass().getDeclaredField("parsers");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Parser> parsers = (List<Parser>) f.get(orParser);
        assertThat("Only one parser is present", parsers.size() == 1);
        assertThat(parsers.get(0), is(equalTo(epsilon)));
    }

    @Test(expected = ParseError.class)
    public void testOrMatchThrowsOnError() {
        initRules("a ::= 'a'");
        new OrParser(Arrays.asList(new RuleParser("a"), new RuleParser("a"))).parse("b");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOrMatchHasNoName() {
        new OrParser(Collections.emptyList()).getName();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOrMatchWithWeirdParameters() {
        new OrParser(Collections.singletonList(new OrParser(Collections.emptyList())));
    }

    @Test
    public void testKleeneMatch() {
        initRules("a ::= 'a'",
                "b ::= 'b'",
                "A ::= a",
                "B ::= b");
        assertThat("KleeneParser parses anything", new KleeneParser(new RuleParser("A")).parses("c"));
        assertThat("KleeneParser parses anything", new KleeneParser(new RuleParser("A")).parses("a"));
        parserTest(new KleeneParser(new RuleParser("a")), "aaaa", "",
                m -> {
                    List<AstNode> list = m.getList();
                    assertThat(list.size(), is(equalTo(4)));
                    list.forEach(child -> assertThat(child.getContentString(), is(equalTo("a"))));
                });
        assertThat(new KleeneParser(new Epsilon()).getName(), is(equalTo("more")));
    }

    private void parserTest(Parser parser, String str, String expectedRight, Consumer<AstNode> consumer) {
        assertThat("Parser " + parser + " can parse " + str, parser.parses(str));
        Pair<AstNode, Pair<String, Position>> parse = parser.parse(str);
        assertThat(parse.getRight().getLeft(), is(equalTo(expectedRight)));
        consumer.accept(parse.getLeft());
    }
}
