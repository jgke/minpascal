package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.util.OptionalUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;
import java.util.function.Function;

@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleStatementNode extends TreeNode {
    private final Optional<ReturnNode> returnNode;
    private final Optional<ReadNode> readNode;
    private final Optional<WriteNode> writeNode;
    private final Optional<AssertNode> assertNode;
    private final Optional<CallNode> callNode;
    private final Optional<AssignmentNode> assignmentNode;

    public <T> T map(
            Function<ReturnNode, T> returnNode,
            Function<ReadNode, T> readNode,
            Function<WriteNode, T> writeNode,
            Function<AssertNode, T> assertNode,
            Function<CallNode, T> callNode,
            Function<AssignmentNode, T> assignmentNode
    ) {
        return OptionalUtils.<T>until()
                .chain(this.returnNode, returnNode)
                .chain(this.readNode, readNode)
                .chain(this.writeNode, writeNode)
                .chain(this.assertNode, assertNode)
                .chain(this.callNode, callNode)
                .chain(this.assignmentNode, assignmentNode)
                .get();
    }
}
