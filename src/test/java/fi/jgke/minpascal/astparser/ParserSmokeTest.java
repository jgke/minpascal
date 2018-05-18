package fi.jgke.minpascal.astparser;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.parsers.RuleMatch;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.util.Pair;
import org.junit.Test;

import static fi.jgke.minpascal.astparser.AstParser.initDefaultParsers;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ParserSmokeTest {
    @Test
    public void simpleTest() {
        initDefaultParsers();
        Pair<AstNode, Pair<String, Position>> parse = AstParser.getRules().get("IdentifierStatement")
                .parse("foo := 5;");
        assertThat(parse.getRight().getLeft(), is(equalTo(";")));
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
        ParserTestUtils.initRules(A, B, C);
        Pair<AstNode, Pair<String, Position>> parse;
        parse = new RuleMatch("A").parse("B");
        assertThat(parse.getRight().getLeft(), is(equalTo("")));
        parse.getLeft().toMap()
                .map("B", $ -> null)
                .map("C", $ -> assertFalse())
                .unwrap();
        parse = new RuleMatch("A").parse("C");
        assertThat(parse.getRight().getLeft(), is(equalTo("")));
        parse.getLeft().toMap()
                .map("C", $ -> null)
                .map("B", $ -> assertFalse())
                .unwrap();
    }

    private static <T> T assertFalse() {
        assertThat(false, is(equalTo(true)));
        return null;
    }

    @Test
    public void testParameters() {
        initDefaultParsers();
        RuleMatch p = new RuleMatch("Parameters");
        p.parse("()").getLeft().debug();
        p.parse("(x: real)").getLeft();
        p.parse("(x: real, y: real)").getLeft();
        p.parse("(x: real, y: real, z: real)").getLeft();
        p.parse("(x: real, y: real, z: real, t: real)").getLeft();
    }
}
