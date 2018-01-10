package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class StructuredStatementNode extends TreeNode {
    private final Optional<BlockNode> blockNode;
    private final Optional<IfThenNode> ifNode;
    private final Optional<WhileNode> whileNode;
}
