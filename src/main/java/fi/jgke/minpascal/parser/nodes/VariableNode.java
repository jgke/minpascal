package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class VariableNode extends TreeNode {
    protected final Token<String> identifier;
    private final Optional<ExpressionNode> arrayAccessInteger;
}
