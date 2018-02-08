package fi.jgke.minpascal.parser.expressions;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.base.ParseUtils;
import fi.jgke.minpascal.parser.nodes.FactorNode;
import fi.jgke.minpascal.parser.nodes.TermNode;

import java.util.Collections;
import java.util.List;

public class Term implements Parsable {
    private Factor factor = new Factor();

    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(factor);
    }

    @Override
    public TermNode parse(ParseQueue queue) {
        FactorNode left = factor.parse(queue);
        if (queue.isNext(ParseUtils.multiplyingOperators)) {
            Token<Void> operator = (Token<Void>) queue.getExpectedToken(ParseUtils.multiplyingOperators);
            FactorNode right = factor.parse(queue);
            return new TermNode(left, operator, right);
        }
        return new TermNode(left);
    }
}
