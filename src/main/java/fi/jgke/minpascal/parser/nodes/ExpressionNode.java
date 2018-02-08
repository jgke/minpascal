package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ExpressionNode extends TreeNode {
    private final SimpleExpressionNode left;
    private final Optional<Token<Void>> operator;
    private final Optional<SimpleExpressionNode> right;

    public ExpressionNode(SimpleExpressionNode left) {
        this(left, Optional.empty(), Optional.empty());
    }

    public ExpressionNode(SimpleExpressionNode left, Token<Void> operator, SimpleExpressionNode right) {
        this.left = left;
        this.right = Optional.of(right);
        this.operator = Optional.of(operator);
    }
}
