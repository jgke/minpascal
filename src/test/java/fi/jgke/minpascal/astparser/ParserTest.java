package fi.jgke.minpascal.astparser;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.parsers.RuleMatch;
import fi.jgke.minpascal.util.Pair;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jgke.minpascal.astparser.AstParser.initDefaultParsers;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ParserTest {
    @Test
    public void simpleTest() {
        initDefaultParsers();
        Pair<AstNode, String> parse = AstParser.getRules().get("IdentifierStatement")
                .parse("foo := 5;");
        assertThat(parse.getRight(), is(equalTo(";")));
        assertThat(parse.getLeft().getFirstChild("identifier").getContentString(),
                is(equalTo("foo")));
        parse.getLeft().getFirstChild("IdentifierStatementContent")
                .getFirstChild("AssignmentStatement");
    }

    @Test
    public void testABCRule() {
        String A = "A ::= B | C";
        String B = "B ::= \"B\"";
        String C = "C ::= \"C\"";
        initRules(A, B, C);
        System.out.println(AstParser.getRules());
        Pair<AstNode, String> parse;
        parse = new RuleMatch("A").parse("B");
        assertThat(parse.getRight(), is(equalTo("")));
        parse.getLeft().debug().toMap()
                .map("B", $ -> null)
                .map("C", $ -> assertFalse())
                .unwrap();
        parse = new RuleMatch("A").parse("C");
        assertThat(parse.getRight(), is(equalTo("")));
        parse.getLeft().debug().toMap()
                .map("C", $ -> null)
                .map("B", $ -> assertFalse())
                .unwrap();
    }

    private static <T> T assertFalse() {
        assertThat(false, is(equalTo(true)));
        return null;
    }

    private static void initRules(String... rules) {
        String ws = "whitespace ::= \" *\"";
        AstParser.setRules(Stream.concat(Arrays.stream(rules), Stream.of(ws)).map(Rule::new)
                .collect(Collectors.toMap(Rule::get_name, Rule::getParser)));
    }
}
