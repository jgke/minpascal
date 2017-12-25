package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class FunctionNode extends TreeNode {
    private final Token identifier;
    private final ParametersNode params;
    private final TreeNode body;
    private final Optional<TreeNode> returnType;

    public FunctionNode(Token identifier, ParametersNode params, TreeNode body, TreeNode returnType) {
        this.identifier = identifier;
        this.params = params;
        this.body = body;
        this.returnType = Optional.of(returnType);
    }

    public FunctionNode(Token identifier, ParametersNode params, TreeNode body) {
        this.identifier = identifier;
        this.params = params;
        this.body = body;
        this.returnType = Optional.empty();
    }
}
