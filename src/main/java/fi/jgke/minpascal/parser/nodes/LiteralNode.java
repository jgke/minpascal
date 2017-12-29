package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class LiteralNode extends TreeNode {
    private final Optional<Integer> integer;
    private final Optional<Double> number;
    private final Optional<String> string;
}
