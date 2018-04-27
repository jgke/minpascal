package fi.jgke.minpascal.compiler.nodes;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import lombok.Data;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Data
public class CBlock {
    @Data
    public class Content {
        private final Optional<CVariable> variableDeclaration;
        private final Optional<CFunction> functionDeclaration;
        private final Optional<Statement> statement;
    }

    private final List<Content> contents;

    public static CBlock parse(AstNode root) {
        root.debug();
        root.toMap()
                .map("ProcedureDeclaration", notImplemented())
                .unwrap();
        throw new RuntimeException("not impl");
        /*
        root.getList().stream()
                .map(statementNode -> null);statementNode.map(
                        simple -> new Content(Optional.empty(), Optional.empty(), Optional.of(Statement.parse(simple))),
                        structuredStatement -> new Content(Optional.empty(), Optional.empty(), Optional.of(Statement.parse(structuredStatement))),
                        declaration -> null
                        )
                );*/
    }

    private static Function<AstNode, Object> notImplemented() {
        return $ -> {
            throw new RuntimeException("not impl");
        };
    }
}
