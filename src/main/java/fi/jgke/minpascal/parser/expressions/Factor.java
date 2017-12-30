package fi.jgke.minpascal.parser.expressions;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.*;
import fi.jgke.minpascal.parser.statements.Call;
import fi.jgke.minpascal.parser.statements.Literal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static fi.jgke.minpascal.data.TokenType.*;

/*
 * Original grammar was not in LL:
 * <factor> ::= <call> | <variable> | <literal> | "(" <expr> ")" | "not" <factor> | < factor> "." "size"
 * so refactor it
 * <factor> ::= <subfactor> <size expression>
 * <subfactor> ::= <variable> (<call> | e) | <literal> | "(" <expr> ")" | "not" <factor>
 * <size expression> ::= "." "size" <size expression> | empty
 */
public class Factor implements Parsable {
    private static final Parsable[] children = new Parsable[]{
            new IdentifierExpression(), new Literal(), new ParenExpression(), new NotFactor()
    };

    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(children);
    }

    @Override
    public FactorNode parse(ParseQueue queue) {
        TreeNode content = queue.any(children);
        CallNode call = null;
        VariableNode variable = null;
        ExpressionNode expression = null;
        NotNode not = null;
        LiteralNode literal = null;
        if (content instanceof CallNode) call = (CallNode) content;
        else if (content instanceof VariableNode) variable = (VariableNode) content;
        else if (content instanceof ExpressionNode) expression = (ExpressionNode) content;
        else if (content instanceof NotNode) not = (NotNode) content;
        else literal = (LiteralNode) content;

        return new FactorNode(
                Optional.ofNullable(call),
                Optional.ofNullable(variable),
                Optional.ofNullable(expression),
                Optional.ofNullable(not),
                Optional.ofNullable(literal),
                new SizeExpression().parseOptional(queue));
    }

    private static class ParenExpression implements Parsable {
        @Override
        public List<Parsable> getParsables() {
            return Collections.singletonList(OPENPAREN);
        }

        @Override
        public TreeNode parse(ParseQueue queue) {
            queue.getExpectedToken(OPENPAREN);
            TreeNode expr = new Expression().parse(queue);
            queue.getExpectedToken(CLOSEPAREN);
            return expr;
        }
    }

    private static class NotFactor implements Parsable {
        @Override
        public List<Parsable> getParsables() {
            return Collections.singletonList(NOT);
        }

        @Override
        public NotNode parse(ParseQueue queue) {
            queue.getExpectedToken(NOT);
            return new NotNode(new Factor().parse(queue));
        }
    }

    /*
     * <factor> := ... | <factor> . size
     * => <factor> T
     *    T := "." "size" T | e
     */
    private static class SizeExpression implements Parsable {
        @Override
        public List<Parsable> getParsables() {
            return Collections.singletonList(DOT);
        }

        public Optional<SizeNode> parseOptional(ParseQueue queue) {
            if (this.matches(queue)) {
                return Optional.of(parse(queue));
            }
            return Optional.empty();
        }

        @Override
        public SizeNode parse(ParseQueue queue) {
            queue.getExpectedTokens(DOT, SIZE);
            return new SizeNode(this.parseOptional(queue));
        }
    }

    private static class IdentifierExpression implements Parsable {
        @Override
        public List<Parsable> getParsables() {
            return Collections.singletonList(IDENTIFIER);
        }

        @Override
        public TreeNode parse(ParseQueue queue) {
            Token identifier = queue.getExpectedToken(IDENTIFIER);
            Call call = new Call();
            if (call.matches(queue)) {
                return call.parseWithIdentifier(identifier, queue);
            }
            return new Variable().parseWithIdentifier(identifier, queue);
        }
    }
}
