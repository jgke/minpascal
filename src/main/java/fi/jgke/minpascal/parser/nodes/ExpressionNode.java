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
    private final TreeNode left;
    private final Optional<Token> operator;
    private final Optional<TreeNode> right;

    public ExpressionNode(TreeNode left) {
        this(left, Optional.empty(), Optional.empty());
    }

    public ExpressionNode(TreeNode left, Token operator, TreeNode right) {
        this.left = left;
        this.right = Optional.of(right);
        this.operator = Optional.of(operator);
    }
}
