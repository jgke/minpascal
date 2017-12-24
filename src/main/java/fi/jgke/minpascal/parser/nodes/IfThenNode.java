package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class IfThenNode extends TreeNode {
    private final TreeNode condition;
    private final TreeNode thenStatement;
    private final Optional<TreeNode> elseStatement;

    public IfThenNode(TreeNode condition, TreeNode thenStatement) {
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = Optional.empty();
    }

    public IfThenNode(TreeNode condition, TreeNode thenStatement, TreeNode elseStatement) {
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = Optional.of(elseStatement);
    }
}
