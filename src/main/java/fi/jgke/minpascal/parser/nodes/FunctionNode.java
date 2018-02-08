package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class FunctionNode extends TreeNode {
    private final Token<String> identifier;
    private final ParametersNode params;
    private final BlockNode body;
    private final Optional<TypeNode> returnType;

    public FunctionNode(Token<String> identifier, ParametersNode params, BlockNode body, TypeNode returnType) {
        this.identifier = identifier;
        this.params = params;
        this.body = body;
        this.returnType = Optional.of(returnType);
    }

    public FunctionNode(Token<String> identifier, ParametersNode params, BlockNode body) {
        this.identifier = identifier;
        this.params = params;
        this.body = body;
        this.returnType = Optional.empty();
    }
}
