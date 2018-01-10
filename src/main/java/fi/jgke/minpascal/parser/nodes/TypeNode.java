package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class TypeNode extends TreeNode {
    private final Optional<SimpleTypeNode> simpleType;
    private final Optional<ArrayTypeNode> arrayType;
}
