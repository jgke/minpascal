package fi.jgke.minpascal.compiler.nodes;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.std.CExpressionResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class CBlock {
    @Data
    public static class Content {
        private final String data;
        /*
        private final Optional<CVariable> variableDeclaration;
        private final Optional<CFunction> functionDeclaration;
        private final Optional<Statement> statement;
        */
    }

    private final List<Content> contents;

    public static CBlock parse(AstNode root) {
        List<Content> content = root.getFirstChild("Statement").<List<Content>>toMap()
                .map("SimpleStatement", CBlock::fromSimple)
                .map("Declaration", notImplemented())
                .map("StructuredStatement", CBlock::fromStructured)
                .unwrap();

        return new CBlock(content);
        /*
        root.getList().stream()
                .map(statementNode -> null);statementNode.map(
                        simple -> new Content(Optional.empty(), Optional.empty(), Optional.of(Statement.parse(simple))),
                        structuredStatement -> new Content(Optional.empty(), Optional.empty(), Optional.of(Statement.parse(structuredStatement))),
                        declaration -> null
                        )
                );*/
    }

    private static List<Content> fromStructured(AstNode astNode) {
        return astNode.<List<Content>>toMap()
                .map("Block", notImplemented())
                .map("If", CBlock::fromIf)
                .map("While", notImplemented())
                .unwrap();
    }

    private static List<Content> fromIf(AstNode astNode) {
        astNode.debug();
        throw new RuntimeException();
    }

    private static List<Content> fromSimple(AstNode simple) {
        return simple.<List<Content>>toMap()
                .map("IdentifierStatement", CBlock::fromIdentifier)
                .map("ReturnStatement", notImplemented())
                .map("AssertStatement", notImplemented())
                .unwrap();
    }

    private static List<Content> fromIdentifier(AstNode identifierStatement) {
        String identifier = identifierStatement
                .getFirstChild("identifier")
                .getContentString();
        if (identifier.equals("writeln")) {
            return fromWrite(identifierStatement
                    .getFirstChild("IdentifierStatementContent")
                    .getFirstChild("Arguments"));
        } else if(identifier.equals("println")) {
            throw new RuntimeException();
        }
        return identifierStatement
                .getFirstChild("IdentifierStatementContent").<List<Content>>toMap()
                .map("AssignmentStatement", notImplemented())
                .map("Arguments", CBlock.fromCall(identifier))
                .unwrap();
    }

    private static List<Content> fromWrite(AstNode writeNode) {
        List<AstNode> astNodes = getArguments(writeNode);
        List<CExpressionResult> collect = astNodes.stream()
                .map(CExpressionResult::fromExpression)
                .collect(Collectors.toList());

        StringBuilder fmt = new StringBuilder();
        List<String> args = new ArrayList<>();
        List<List<String>> steps = new ArrayList<>();
        List<String> post = new ArrayList<>();

        for (CExpressionResult result : collect) {
            fmt.append(result.getType().toFormat()).append(" ");
            args.add(result.getIdentifier());
            steps.add(result.getTemporaries());
            post.addAll(result.getPost());
        }

        String pre = steps.stream()
                .map(list -> list.stream()
                        .collect(Collectors.joining("\n")) + "\n")
                .collect(Collectors.joining("\n")) + "\n";
        String print = "printf(\"" + fmt.toString().trim() + "\\n\", " + args.stream().collect(Collectors.joining(", ")) + ");\n";
        String clean = post.stream().collect(Collectors.joining("\n")) + "\n";
        return Collections.singletonList(new Content(pre + print + clean));
    }

    private static Function<AstNode, List<Content>> fromCall(String identifier) {
        return argumentsNode -> {
            List<AstNode> astNodes = getArguments(argumentsNode);
            List<CExpressionResult> collect = astNodes.stream()
                    .map(CExpressionResult::fromExpression)
                    .collect(Collectors.toList());

            List<String> args = new ArrayList<>();
            List<List<String>> steps = new ArrayList<>();
            List<String> post = new ArrayList<>();

            for (CExpressionResult result : collect) {
                args.add(result.getIdentifier());
                steps.add(result.getTemporaries());
                post.addAll(result.getPost());
            }

            String pre = steps.stream()
                    .map(list -> list.stream()
                            .collect(Collectors.joining("\n")) + "\n")
                    .collect(Collectors.joining("\n")) + "\n";
            String print = identifier + "(" + args.stream().collect(Collectors.joining(", ")) + ");\n";
            String clean = post.stream().collect(Collectors.joining("\n")) + "\n";
            return Collections.singletonList(new Content(pre + print + clean));
        };
    }

    private static List<AstNode> getArguments(AstNode argumentsNode) {
        return argumentsNode
                .getFirstChild("Arguments")
                .getFirstChild("Expression")
                .toOptional().map(
                        node -> {
                            AstNode expression = node.getFirstChild("Expression");
                            List<AstNode> more = expression.getFirstChild("more").getList();
                            List<AstNode> expressions = new ArrayList<>();
                            expressions.add(node.getFirstChild("Expression").getFirstChild("Expression"));
                            expressions.addAll(more);
                            return expressions;
                        }
                ).orElse(Collections.emptyList());
    }

    private static <T> Function<AstNode, T> notImplemented() {
        return $ -> {
            throw new RuntimeException("not impl " + $.getName());
        };
    }
}
