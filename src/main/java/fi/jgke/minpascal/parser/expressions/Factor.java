package fi.jgke.minpascal.parser.expressions;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.FactorNode;
import fi.jgke.minpascal.parser.nodes.NotNode;
import fi.jgke.minpascal.parser.nodes.SizeNode;
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
        return new FactorNode(content, new SizeExpression().parseOptional(queue));
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
        public boolean matches(ParseQueue queue) {
            return true;
        }

        @Override
        public List<TokenType> getMatchableTokens() {
            return Collections.emptyList();
        }

        @Override
        public List<Parsable> getParsables() {
            throw new CompilerException("getParsables() called on SizeExpression");
        }

        public Optional<SizeNode> parseOptional(ParseQueue queue) {
            if (queue.isNext(DOT)) {
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
            if(call.matches(queue)) {
                return call.parse(queue);
            }
            return new Variable().parseWithIdentifier(identifier, queue);
        }
    }
}
