package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.util.OptionalUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;
import java.util.function.Function;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeclarationNode extends TreeNode {
    private final Optional<VarDeclarationNode> varDeclaration;
    private final Optional<FunctionNode> functionNode;

    public <T> T map(
            Function<VarDeclarationNode, T> varDeclaration,
            Function<FunctionNode, T> function
    ) {
        return OptionalUtils.<T>until()
                .chain(this.varDeclaration, varDeclaration)
                .chain(this.functionNode, function)
                .get();
    }
}
