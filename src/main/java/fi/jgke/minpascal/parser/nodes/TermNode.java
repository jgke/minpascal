package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class TermNode extends TreeNode {
    private final FactorNode left;
    private final Optional<Token<Void>> operator;
    private final Optional<FactorNode> right;

    public TermNode(FactorNode left, Token<Void> operator, FactorNode right) {
        this.left = left;
        this.operator = Optional.of(operator);
        this.right = Optional.of(right);
    }

    public TermNode(FactorNode left) {
        this.left = left;
        this.operator = Optional.empty();
        this.right = Optional.empty();
    }
}
