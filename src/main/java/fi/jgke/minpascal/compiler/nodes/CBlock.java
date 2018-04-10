package fi.jgke.minpascal.compiler.nodes;

import fi.jgke.minpascal.parser.nodes.BlockNode;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class CBlock {
    @Data
    private class Content {
        private final Optional<CVariable> variableDeclaration;
        private final Optional<CFunction> functionDeclaration;
        private final Optional<Statement> statement;
    }

    private final List<Content> contents;

    public static CBlock parse(BlockNode root) {
        root.getChildren().stream()
                .map(statementNode -> null);/*statementNode.map(
                        simple -> new Content(Optional.empty(), Optional.empty(), Optional.of(Statement.parse(simple))),
                        structuredStatement -> new Content(Optional.empty(), Optional.empty(), Optional.of(Statement.parse(structuredStatement))),
                        declaration -> null
                        )
                );*/
        return null;
    }
}
