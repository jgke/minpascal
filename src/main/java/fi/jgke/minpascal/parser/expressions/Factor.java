package fi.jgke.minpascal.parser.expressions;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.exception.ParseException;
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

import static fi.jgke.minpascal.data.TokenType.*;

public class Factor implements Parsable {
    private static final Parsable[] children = new Parsable[]{
            new Call(), new Variable(), new Literal(), new ParenExpression(), new NotFactor(), new SizeExpression()
    };

    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(children);
    }

    @Override
    public FactorNode parse(ParseQueue queue) {
        return new FactorNode(queue.any(children));
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

    private static class SizeExpression implements Parsable {
        @Override
        public List<Parsable> getParsables() {
            return Collections.singletonList(new Factor());
        }

        @Override
        public TreeNode parse(ParseQueue queue) {
            FactorNode factor = new Factor().parse(queue);
            queue.getExpectedToken(DOT);
            Token sizeToken = queue.getExpectedToken(IDENTIFIER);
            String sizeTokenContent = (String) sizeToken.getValue()
                    .orElseThrow(() -> new CompilerException("Identifier didn't have any content"));
            if (!sizeTokenContent.equals("size")) {
                throw new ParseException("Factor operation '" + sizeTokenContent + "' not supported");
            }
            return new SizeNode(factor);
        }
    }
}
