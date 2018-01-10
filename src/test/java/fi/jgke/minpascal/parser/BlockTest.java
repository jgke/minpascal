package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.blocks.Block;
import fi.jgke.minpascal.parser.blocks.Declaration;
import fi.jgke.minpascal.parser.blocks.Statement;
import fi.jgke.minpascal.parser.blocks.Type;
import fi.jgke.minpascal.parser.nodes.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
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
    private final Token bool = new Token(IDENTIFIER, Optional.of("boolean"), new Position(0, 0));
    private final Token var = new Token(VAR, Optional.empty(), new Position(0, 0));
    private final Token colon = new Token(COLON, Optional.empty(), new Position(0, 0));
    private final Token array = new Token(ARRAY, Optional.empty(), new Position(0, 0));
    private final Token ob = new Token(OPENBRACKET, Optional.empty(), new Position(0, 0));
    private final Token cb = new Token(CLOSEBRACKET, Optional.empty(), new Position(0, 0));
    private final Token of = new Token(OF, Optional.empty(), new Position(0, 0));
    private final Token procedure = new Token(PROCEDURE, Optional.empty(), new Position(0, 0));
    private final Token op = new Token(OPENPAREN, Optional.empty(), new Position(0, 0));
    private final Token cp = new Token(CLOSEPAREN, Optional.empty(), new Position(0, 0));
    private final Token semicolon = new Token(SEMICOLON, Optional.empty(), new Position(0, 0));
    private final Token dot = new Token(DOT, Optional.empty(), new Position(0, 0));
    private final Token function = new Token(FUNCTION, Optional.empty(), new Position(0, 0));
    private final Token integer = new Token(IDENTIFIER, Optional.of("integer"), new Position(0, 0));

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

    @Test
    public void testSimpleType() {
        ParseQueue queue = queueWith(bool);
        TypeNode parse = new Type().parse(queue);
        assertThat("Simple type is present", parse.getSimpleType().isPresent());
        assertThat("type is boolean",
                parse.getSimpleType().get().getType(),
                is(equalTo(bool)));
    }

    @Test
    public void testArrayType() {
        ParseQueue queue = queueWith(array, ob, five, cb, of, bool);
        TypeNode parse = new Type().parse(queue);
        assertThat("Array type is present", parse.getArrayType().isPresent());
        assertThat("Array size is five",
                parse.getArrayType().get().getSize().getLeft().getLeft().getLeft().getLiteral().get().getInteger().get(),
                is(equalTo(5)));
        assertThat("Array type is boolean",
                parse.getArrayType().get().getType().getType(),
                is(equalTo(bool)));
    }

    @Test
    public void testProcedure() {
        ParseQueue queue = queueWith(procedure, id, op, id, colon, bool, cp, semicolon, begin, id, assign, five, end, dot);
        testFunction(queue, Optional.empty());
    }

    @Test
    public void testVarDeclaration() {
        ParseQueue queue = queueWith(var, id, colon, integer);
        DeclarationNode parse = new Declaration().parse(queue);
        assertThat("Declaration node is present", parse.getVarDeclaration().isPresent());
        assertThat("Declaration node contains id",
                parse.getVarDeclaration().get().getIdentifiers(),
                is(equalTo(Collections.singletonList(id))));
        assertThat("Declaration type is integer",
                parse.getVarDeclaration().get().getType().getSimpleType().get().getType(),
                is(equalTo(integer)));
    }

    @Test
    public void testMultiVarDeclaration() {
        ParseQueue queue = queueWith(var, id, comma, id, colon, bool);
        DeclarationNode parse = new Declaration().parse(queue);
        assertThat("Declaration node is present", parse.getVarDeclaration().isPresent());
        assertThat("Declaration node contains id, id",
                parse.getVarDeclaration().get().getIdentifiers(),
                is(equalTo(Arrays.asList(id, id))));
        assertThat("Declaration type is boolean",
                parse.getVarDeclaration().get().getType().getSimpleType().get().getType(),
                is(equalTo(bool)));
    }

    @Test
    public void testFunction() {
        ParseQueue queue = queueWith(function, id, op, id, colon, bool, cp, colon, integer, semicolon, begin, id, assign, five, end, dot);
        testFunction(queue, Optional.of(integer));
    }

    private void testFunction(ParseQueue queue, Optional<Token> expectedType) {
        StatementNode parse = new Statement().parse(queue);
        assertThat("Procedure node is present", parse.getDeclarationNode().get().getFunctionNode().isPresent());
        assertThat("Procedure arguments' size is 1",
                parse.getDeclarationNode().get().getFunctionNode().get().getParams().getDeclarations().size(),
                is(equalTo(1)));
        assertThat("Procedure arguments contains id : bool",
                parse.getDeclarationNode().get().getFunctionNode().get().getParams().getDeclarations().get(0).getIdentifiers().get(0),
                is(equalTo(id)));
        assertThat("Procedure arguments contains id : bool",
                parse.getDeclarationNode().get().getFunctionNode().get().getParams().getDeclarations().get(0).getType().getSimpleType().get().getType(),
                is(equalTo(bool)));
        assertThat("Procedure return value is as expected",
                parse.getDeclarationNode().get().getFunctionNode().get().getReturnType().map(type -> type.getSimpleType().get().getType()),
                is(equalTo(expectedType)));
        assertThat("Procedure body contains assignment",
                parse.getDeclarationNode().get().getFunctionNode().get().getBody().getChildren().size(),
                is(equalTo(1)));
        assertThat("Procedure body contains assignment",
                parse.getDeclarationNode().get().getFunctionNode().get().getBody().getChildren().get(0).getSimple().get().getAssignmentNode().isPresent());
    }
}
