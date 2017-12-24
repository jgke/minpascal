package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VariableNode extends TreeNode {
    private final Token identifier;
}
