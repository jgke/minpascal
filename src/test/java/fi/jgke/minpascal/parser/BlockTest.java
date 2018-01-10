package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.blocks.Block;
import fi.jgke.minpascal.parser.nodes.AssignmentNode;
import fi.jgke.minpascal.parser.nodes.BlockNode;
import fi.jgke.minpascal.parser.nodes.IfThenNode;
import org.junit.Test;

import java.util.Optional;

import static fi.jgke.minpascal.TestUtils.queueWith;
import static fi.jgke.minpascal.data.TokenType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@SuppressWarnings("ConstantConditions")
public class BlockTest {

    private final Token five = new Token(INTEGER_LITERAL, Optional.of(5), new Position(0, 0));
    private final Token If = new Token(IF, Optional.empty(), new Position(0, 0));
    private final Token True = new Token(TRUE, Optional.empty(), new Position(0, 0));
    private final Token then = new Token(THEN, Optional.empty(), new Position(0, 0));
    private final Token comma = new Token(COMMA, Optional.empty(), new Position(0, 0));
    private final Token id = new Token(IDENTIFIER, Optional.of("foobar"), new Position(0, 0));
    private final Token assign = new Token(ASSIGN, Optional.empty(), new Position(0, 0));
    private final Token returnToken = new Token(RETURN, Optional.empty(), new Position(0, 0));
    private final Token read = new Token(IDENTIFIER, Optional.of("read"), new Position(0, 0));
    private final Token write = new Token(IDENTIFIER, Optional.of("writeln"), new Position(0, 0));
    private final Token assertToken = new Token(ASSERT, Optional.empty(), new Position(0, 0));
    private final Token begin = new Token(BEGIN, Optional.empty(), new Position(0, 0));
    private final Token end = new Token(END, Optional.empty(), new Position(0, 0));

    @Test
    public void testBlock() {
        ParseQueue queue = queueWith(begin, id, assign, five, end);
        BlockNode parse = new Block().parse(queue);
        assertThat("One child is present", parse.getChildren().size(), is(equalTo(1)));
        Optional<AssignmentNode> node = parse.getChildren().get(0).getSimple().get().getAssignmentNode();
        assertThat("Assignment expression is present", node.isPresent());
        assertThat("Assignment identifier contains foobar",
                node.get().getIdentifier().getIdentifier().getValue().get(),
                is(equalTo("foobar")));
        assertThat("Array assignment is not present",
                !node.get().getIdentifier().getArrayAccessInteger().isPresent());
        assertThat("Assignment expression contain 5",
                node.get().getValue().getLeft().getLeft().getLeft().getLiteral().get().getInteger().get(),
                is(equalTo(5)));
    }

    @Test
    public void testIf() {
        ParseQueue queue = queueWith(begin, If, True, then, id, assign, five, end);
        BlockNode parse = new Block().parse(queue);
        System.out.println(parse);
        assertThat("One child is present", parse.getChildren().size(), is(equalTo(1)));
        Optional<IfThenNode> node = parse.getChildren().get(0).getStructured().get().getIfNode();
        assertThat("if node is present", node.isPresent());
        assertThat("if condition contains true",
                node.get().getCondition().getLeft().getLeft().getLeft().getLiteral().get().getBool().get(),
                is(equalTo(true)));
        assertThat("if block contains assign",
                node.get().getThenStatement().getSimple().get().getAssignmentNode().isPresent(),
                is(equalTo(true)));
    }
}
