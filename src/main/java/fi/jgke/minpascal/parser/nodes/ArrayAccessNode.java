package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.Token;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArrayAccessNode extends VariableNode {
    private final ExpressionNode integerExpression;

    public ArrayAccessNode(Token identifier, ExpressionNode integerExpression) {
        super(identifier);
        this.integerExpression = integerExpression;
    }
}
