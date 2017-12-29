package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class SizeNode extends TreeNode {
    // This being present causes a type error :)
    private final Optional<SizeNode> sizeExpression;
}
