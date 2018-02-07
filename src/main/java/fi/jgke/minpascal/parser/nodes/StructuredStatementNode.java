package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.util.OptionalUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;
import java.util.function.Function;

import static fi.jgke.minpascal.util.OptionalUtils.assertOne;

@Data
@EqualsAndHashCode(callSuper = true)
public class StructuredStatementNode extends TreeNode {
    private final Optional<BlockNode> blockNode;
    private final Optional<IfThenNode> ifNode;
    private final Optional<WhileNode> whileNode;

    public StructuredStatementNode(Optional<BlockNode> blockNode, Optional<IfThenNode> ifNode, Optional<WhileNode> whileNode) {
        this.blockNode = blockNode;
        this.ifNode = ifNode;
        this.whileNode = whileNode;

        assertOne(blockNode, ifNode, whileNode);
    }

    public <T> T map(
            Function<BlockNode, T> blockNode,
            Function<IfThenNode, T> ifNode,
            Function<WhileNode, T> whileNode
    ) {
        return OptionalUtils.<T>until()
                .chain(this.blockNode, blockNode)
                .chain(this.ifNode, ifNode)
                .chain(this.whileNode, whileNode)
                .get();
    }
}
