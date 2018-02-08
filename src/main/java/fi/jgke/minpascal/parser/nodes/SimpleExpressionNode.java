package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleExpressionNode extends TreeNode {
    private final Optional<Token<Void>> sign;
    private final TermNode left;
    private final Optional<Token<Void>> addingOperator;
    private final Optional<TermNode> right;

    public SimpleExpressionNode(Optional<Token<Void>> sign, TermNode left) {
        this.sign = sign;
        this.left = left;
        this.addingOperator = Optional.empty();
        this.right = Optional.empty();
    }

    public SimpleExpressionNode(Optional<Token<Void>> sign, TermNode left, Token<Void> addingOperator, TermNode right) {
        this.sign = sign;
        this.left = left;
        this.addingOperator = Optional.of(addingOperator);
        this.right = Optional.of(right);
    }
}
