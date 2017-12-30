package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class StatementNode extends TreeNode {
    private final Optional<SimpleStatementNode> simple;
    private final Optional<StructuredStatementNode> structured;
    private final Optional<DeclarationNode> declarationNode;
}
