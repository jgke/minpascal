package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.util.OptionalUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;
import java.util.function.Function;

@Data
@EqualsAndHashCode(callSuper = true)
public class StatementNode extends TreeNode {
    private final Optional<SimpleStatementNode> simple;
    private final Optional<StructuredStatementNode> structured;
    private final Optional<DeclarationNode> declarationNode;

    public <T> T map(Function<SimpleStatementNode, T> simple,
                     Function<StructuredStatementNode, T> structured,
                     Function<DeclarationNode, T> declaration) {
        return OptionalUtils.<T>until()
                .chain(this.simple, simple)
                .chain(this.structured, structured)
                .chain(this.declarationNode, declaration)
                .get();
    }
}
