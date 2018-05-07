package fi.jgke.minpascal.compiler.nodes;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.compiler.IdentifierContext;
import fi.jgke.minpascal.compiler.std.CExpressionResult;
import fi.jgke.minpascal.util.Pair;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jgke.minpascal.compiler.std.CExpressionResult.formatExpressions;
import static fi.jgke.minpascal.compiler.std.CExpressionResult.getArguments;

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
        IdentifierContext.push();
        Stream<Content> content = fromStatement(root.getFirstChild("Statement"));
        Stream<Content> moreStatements = root.getFirstChild("more").toOptional()
                .flatMap(mmore -> mmore.toOptional()
                        .map(more -> more
                                .getList().stream().flatMap(s -> s
                                        .getFirstChild("Statement")
                                        .toOptional().map(m -> m.getList().stream()
                                                .map(AstNode::toOptional)
                                                .flatMap(inner -> inner
                                                        .map(CBlock::fromStatement)
                                                        .orElse(Stream.empty())))
                                        .orElse(Stream.empty()))))
                .orElse(Stream.empty());
        List<Content> collect = Stream.concat(content, moreStatements)
                .collect(Collectors.toList());
        IdentifierContext.pop();

        return new CBlock(collect);
    }

    public static Stream<Content> fromStatement(AstNode statement) {
        return statement.<Stream<Content>>toMap()
                .map("SimpleStatement", CBlock::fromSimple)
                .map("Declaration", CBlock::fromDeclaration)
                .map("StructuredStatement", CBlock::fromStructured)
                .unwrap();
    }

    private static Stream<Content> fromDeclaration(AstNode astNode) {
        return astNode.<Stream<Content>>toMap()
                .map("VarDeclaration", CBlock::varDeclaration)
                .map("ProcedureDeclaration", CBlock::procedureDeclaration)
                .map("FunctionDeclaration", CBlock::functionDeclaration)
                .unwrap();
    }

    private static Stream<Content> procedureDeclaration(AstNode astNode) {
        return functionDeclaration(CType.CVOID).apply(astNode);
    }

    private static Stream<Content> functionDeclaration(AstNode node) {
        CType type = CType.fromTypeNode(node.getFirstChild("Type"));
        return functionDeclaration(type).apply(node);
    }

    private static Function<AstNode, Stream<Content>> functionDeclaration(CType returnType) {
        return node -> {
            List<Pair<String, CType>> collect = Stream.concat(
                    node.getFirstChild("Parameters")
                            .getFirstChild("Parameter")
                            .getFirstChild("Parameter").toOptional()
                            .map(p -> p.getFirstChild("Parameter").getFirstChild("var"))
                            .map(Collections::singletonList)
                            .orElse(Collections.emptyList())
                            .stream(),
                    node.getFirstChild("Parameters")
                            .getFirstChild("Parameter")
                            .getFirstChild("Parameter").toOptional()
                            .map(m -> m
                                    .getFirstChild("more").getList().stream())
                            .orElse(Stream.empty())
            ).map(n -> new Pair<>(n.getFirstChild("identifier")
                    .getFirstChild("identifier")
                    .getContentString(),
                    CType.fromTypeNode(n.getFirstChild("identifier").getFirstChild("Type"))
            )).collect(Collectors.toList());
            List<CType> parameters = collect.stream().map(Pair::getRight).collect(Collectors.toList());
            CType ftype = new CType(returnType, parameters);
            String identifier = node.getFirstChild("identifier").getContentString();
            IdentifierContext.addIdentifier(identifier, ftype);
            IdentifierContext.push();
            collect.forEach(p -> IdentifierContext.addIdentifier(p.getLeft(), p.getRight()));
            Stream<Content> block = Stream.of(new Content("void " + identifier + "("
                    + collect.stream().map(p -> p.getRight().toDeclaration(p.getLeft()))
                    .collect(Collectors.joining(", "))
                    + ") {\n" +
                    format(CBlock.parse(node.getFirstChild("Block")).contents.stream()) +
                    "\n}\n"));
            IdentifierContext.pop();
            return block;
        };
    }

    private static Stream<Content> varDeclaration(AstNode astNode) {
        String identifier = astNode.getFirstChild("identifier").getContentString();
        CType type = CType.fromTypeNode(astNode.getFirstChild("Type"));
        List<String> more = astNode.getFirstChild("more").toOptional()
                .map(t -> t.getList().stream()
                        .map(c -> c.getFirstChild("identifier").getContentString()))
                .orElse(Stream.empty())
                .collect(Collectors.toList());
        Stream.concat(Stream.of(identifier), more.stream())
                .forEach(ident -> IdentifierContext.addIdentifier(ident, type));
        return Stream.concat(Stream.of(identifier), more.stream())
                .map(ident -> new Content(type.toDeclaration(ident) + ";"));
    }

    private static Stream<Content> fromStructured(AstNode astNode) {
        return astNode.<Stream<Content>>toMap()
                .map("Block", notImplemented())
                .map("If", CBlock::fromIf)
                .map("While", CBlock::fromWhile)
                .unwrap();
    }

    private static Stream<Content> fromWhile(AstNode astNode) {
        AstNode condition = astNode.getFirstChild("Expression");
        AstNode body = astNode.getFirstChild("Statement");
        CExpressionResult conditionResult = CExpressionResult.fromExpression(condition);
        String whileStart = IdentifierContext.genIdentifier("whileStart");
        String whileEnd = IdentifierContext.genIdentifier("whileEnd");
        String whileBlock = formatExpressions(
                Collections.singletonList(conditionResult),
                $ -> String.format("%s: if(!%s) goto %s;\n", whileStart, conditionResult.getIdentifier(), whileEnd)
                        + format(fromStatement(body))
                        + "goto " + whileStart + ";\n"
                        + whileEnd + ":;");
        return Stream.of(new Content(whileBlock));
    }

    private static Stream<Content> fromIf(AstNode astNode) {
        AstNode condition = astNode.getFirstChild("Expression");
        AstNode body = astNode.getFirstChild("Statement");
        Stream<Content> elseBody = astNode.getFirstChild("else")
                .toOptional().map(m -> m.getFirstChild("else").getFirstChild("Statement"))
                .map(CBlock::fromStatement)
                .orElse(Stream.empty());
        CExpressionResult conditionResult = CExpressionResult.fromExpression(condition);
        String ifFalse = IdentifierContext.genIdentifier("ifFalse");
        String ifEnd = IdentifierContext.genIdentifier("ifEnd");
        String ifBlock = formatExpressions(
                Collections.singletonList(conditionResult),
                $ -> String.format("if(!%s) goto %s;\n", conditionResult.getIdentifier(), ifFalse)
                        + format(fromStatement(body))
                        + "goto " + ifEnd + ";"
                        + ifFalse + ":;"
                        + format(elseBody)
                        + ifEnd + ":;");
        return Stream.of(new Content(ifBlock));
    }

    private static Stream<Content> fromSimple(AstNode simple) {
        return simple.<Stream<Content>>toMap()
                .map("IdentifierStatement", CBlock::fromIdentifier)
                .map("ReturnStatement", CBlock::fromReturn)
                .map("AssertStatement", notImplemented())
                .unwrap();
    }

    private static Stream<Content> fromReturn(AstNode astNode) {
        CExpressionResult result = CExpressionResult.fromExpression(astNode.getFirstChild("Expression"));
        return Stream.of(new Content(
                result.getTemporaries().stream().collect(Collectors.joining(";\n"))
                        + "return " + result.getIdentifier() + ";\n"
        ));
    }

    private static Stream<Content> fromIdentifier(AstNode identifierStatement) {
        String identifier = identifierStatement
                .getFirstChild("identifier")
                .getContentString();
        if (identifier.equals("writeln")) {
            return fromWrite(identifierStatement
                    .getFirstChild("IdentifierStatementContent")
                    .getFirstChild("Arguments"));
        } else if (identifier.equals("println")) {
            throw new RuntimeException();
        }
        return identifierStatement
                .getFirstChild("IdentifierStatementContent").<Stream<Content>>toMap()
                .map("AssignmentStatement", CBlock.assignmentStatement(identifier))
                .map("Arguments", CBlock.fromCall(identifier))
                .unwrap();
    }

    private static Function<AstNode, Stream<Content>> assignmentStatement(String identifier) {
        return assignmentStatement -> assignmentStatement
                .getFirstChild("ob")
                .getOptionalChild("ob")
                .<Stream<Content>>flatMap(ob -> {
                    ob.toOptional().map(o -> notImplemented().apply(o));
                    return Optional.empty();
                })
                .orElseGet(() -> {
                    CExpressionResult expression = CExpressionResult.fromExpression(assignmentStatement.getFirstChild("ob")
                            .getFirstChild("assign")
                            .getFirstChild("Expression"));
                    return Stream.concat(expression.getTemporaries().stream(),
                            Stream.of(identifier + " = " + expression.getIdentifier() + ";"))
                            .map(Content::new);
                });
    }

    private static Stream<Content> fromWrite(AstNode writeNode) {
        List<CExpressionResult> collect = getArguments(writeNode.getFirstChild("Arguments"));

        String fmt = collect.stream()
                .map(result -> result.getType().toFormat() + " ")
                .collect(Collectors.joining());

        String print = "printf(\"" + fmt.trim() + "\\n\", " + collect.stream()
                .map(CExpressionResult::getIdentifier)
                .collect(Collectors.joining(", ")) + ");\n";
        String total = formatExpressions(collect, $ -> print);
        return Stream.of(new Content(total));
    }

    private static Function<AstNode, Stream<Content>> fromCall(String identifier) {
        return argumentsNode -> {
            List<CExpressionResult> collect = getArguments(argumentsNode);

            String total = formatExpressions(
                    collect,
                    expressions -> identifier + "(" + expressions.stream()
                            .map(CExpressionResult::getIdentifier)
                            .collect(Collectors.joining(", ")) + ");\n");

            return Stream.of(new Content(total));
        };
    }

    private static <T> Function<AstNode, T> notImplemented() {
        return $ -> {
            throw new RuntimeException("not impl " + $.getName());
        };
    }

    private static String format(Stream<Content> blocks) {
        return blocks
                .map(c -> c.data)
                .collect(Collectors.joining());
    }
}
