package fi.jgke.minpascal.parser.expressions;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.ExpressionNode;
import fi.jgke.minpascal.parser.nodes.VariableNode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static fi.jgke.minpascal.data.TokenType.*;

public class Variable implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(IDENTIFIER);
    }

    @Override
    public VariableNode parse(ParseQueue queue) {
       Token<String> identifier = queue.getIdentifier();
        return parseWithIdentifier(identifier, queue);
    }

    public VariableNode parseWithIdentifier(Token<String> identifier, ParseQueue queue) {
        if(queue.isNext(OPENBRACKET)) {
            queue.getExpectedToken(OPENBRACKET);
            ExpressionNode integerExpression = new Expression().parse(queue);
            queue.getExpectedToken(CLOSEBRACKET);
            return new VariableNode(identifier, Optional.ofNullable(integerExpression));
        }
        return new VariableNode(identifier, Optional.empty());
    }
}
