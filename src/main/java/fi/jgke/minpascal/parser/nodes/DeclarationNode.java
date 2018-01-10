package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeclarationNode extends TreeNode {
    private final Optional<VarDeclarationNode> varDeclaration;
    private final Optional<FunctionNode> functionNode;
}
