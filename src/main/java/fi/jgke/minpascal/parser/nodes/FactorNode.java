package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class FactorNode extends TreeNode {
    private final Optional<CallNode> call;
    private final Optional<VariableNode> variable;
    private final Optional<ExpressionNode> expression;
    private final Optional<NotNode> not;
    private final Optional<LiteralNode> literal;

    private final Optional<SizeNode> sizeExpression;
}
