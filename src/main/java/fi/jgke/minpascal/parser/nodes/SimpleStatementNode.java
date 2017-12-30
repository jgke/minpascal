package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleStatementNode extends TreeNode {
    private final Optional<ReturnNode> returnNode;
    private final Optional<ReadNode> readNode;
    private final Optional<WriteNode> writeNode;
    private final Optional<AssertNode> assertNode;
    private final Optional<CallNode> callNode;
    private final Optional<AssignmentNode> assignmentNode;
}
