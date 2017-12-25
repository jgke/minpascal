package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArrayTypeNode extends TreeNode {
    private final ExpressionNode size;
    private final SimpleTypeNode type;
}
