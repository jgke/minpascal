package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class TermNode extends TreeNode {
    private final TreeNode left;
    private final Optional<Token> operator;
    private final Optional<TreeNode> right;

    public TermNode(TreeNode left, Token operator, TreeNode right) {
        this.left = left;
        this.operator = Optional.of(operator);
        this.right = Optional.of(right);
    }

    public TermNode(TreeNode left) {
        this.left = left;
        this.operator = Optional.empty();
        this.right = Optional.empty();
    }
}
