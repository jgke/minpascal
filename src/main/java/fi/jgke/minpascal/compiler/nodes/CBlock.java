package fi.jgke.minpascal.compiler.nodes;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import lombok.Data;

import java.util.Collections;
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
        root.getFirstChild("Statement").debug();

        Content content = root.getFirstChild("Statement").<Content>toMap()
                .map("SimpleStatement", CBlock::fromSimple)
                .map("Declaration", notImplemented())
                .map("StructuredStatement", notImplemented())
                .unwrap();

        return new CBlock(Collections.singletonList(content));
        /*
        root.getList().stream()
                .map(statementNode -> null);statementNode.map(
                        simple -> new Content(Optional.empty(), Optional.empty(), Optional.of(Statement.parse(simple))),
                        structuredStatement -> new Content(Optional.empty(), Optional.empty(), Optional.of(Statement.parse(structuredStatement))),
                        declaration -> null
                        )
                );*/
    }

    private static Content fromSimple(AstNode simple) {
        return simple.<Content>toMap()
                .map("IdentifierStatement", notImplemented())
                .unwrap();
    }

    private static <T> Function<AstNode, T> notImplemented() {
        return $ -> {
            throw new RuntimeException("not impl " + $.getName());
        };
    }
}
