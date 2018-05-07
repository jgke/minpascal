package fi.jgke.minpascal.astparser;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.parsers.RuleMatch;
import fi.jgke.minpascal.util.Pair;
import org.junit.Before;
import org.junit.Test;

import static fi.jgke.minpascal.astparser.AstParser.initDefaultParsers;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DefaultRulesTest {
    @Before
    public void initDefault() {
        initDefaultParsers();
    }

    @Test
    public void literalTest() {
        Pair<AstNode, String> parse = new RuleMatch("stringliteral").parse("\"foobar\\\"baz\"");
        assertThat(parse.getRight(), is(equalTo("")));
        assertThat(parse.getLeft().getContentString(), is(equalTo("\"foobar\\\"baz\"")));
    }
}
