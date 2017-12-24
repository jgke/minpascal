package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class VarDeclarationNode extends TreeNode {
    private final List<Token> identifiers;
    private final TreeNode type;
}
