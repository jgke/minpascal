package fi.jgke.minpascal.parser.expressions;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.SimpleExpressionNode;
import fi.jgke.minpascal.parser.nodes.TermNode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static fi.jgke.minpascal.data.TokenType.MINUS;
import static fi.jgke.minpascal.data.TokenType.PLUS;
import static fi.jgke.minpascal.parser.base.ParseUtils.addingOperators;

public class SimpleExpression implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(PLUS, MINUS, new Term());
    }

    @Override
    public SimpleExpressionNode parse(ParseQueue queue) {
        Optional<Token<Void>> sign = Optional.empty();
        if (queue.isNext(PLUS, MINUS)) {
            sign = Optional.of((Token<Void>) queue.getExpectedToken(PLUS, MINUS));
        }

        TermNode left = new Term().parse(queue);

        if (queue.isNext(addingOperators)) {
            Token<Void> operator = (Token<Void>) queue.getExpectedToken(addingOperators);
            TermNode right = new Term().parse(queue);
            return new SimpleExpressionNode(sign, left, operator, right);
        }

        return new SimpleExpressionNode(sign, left);
    }
}
