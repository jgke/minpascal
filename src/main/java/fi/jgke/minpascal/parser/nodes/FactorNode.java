package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.util.OptionalUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;
import java.util.function.Function;

@Data
@EqualsAndHashCode(callSuper = true)
public class FactorNode extends TreeNode {
    private final Optional<CallNode> call;
    private final Optional<VariableNode> variable;
    private final Optional<ExpressionNode> expression;
    private final Optional<NotNode> not;
    private final Optional<LiteralNode> literal;
    private final Optional<SizeNode> sizeExpression;

    public <T> T map(
            Function<CallNode, T> call,
            Function<VariableNode, T> variable,
            Function<ExpressionNode, T> expression,
            Function<NotNode, T> not,
            Function<LiteralNode, T> literal,
            Function<SizeNode, T> size
    ) {
        return OptionalUtils.<T>until()
                .chain(this.call, call)
                .chain(this.variable, variable)
                .chain(this.expression, expression)
                .chain(this.not, not)
                .chain(this.literal, literal)
                .chain(this.sizeExpression, size)
                .get();
    }
}
