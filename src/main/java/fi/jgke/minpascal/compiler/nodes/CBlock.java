package fi.jgke.minpascal.compiler.nodes;

import com.google.common.collect.Streams;
import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.compiler.IdentifierContext;
import fi.jgke.minpascal.compiler.std.CExpressionResult;
import fi.jgke.minpascal.exception.TypeError;
import fi.jgke.minpascal.util.Pair;
import lombok.Data;

import java.util.ArrayList;
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
        IdentifierContext.setLastStatementWasReturn(false);
        return statement.<Stream<Content>>toMap()
                .map("SimpleStatement", CBlock::fromSimple)
                .map("Declaration", CBlock::fromDeclaration)
                .map("StructuredStatement", CBlock::fromStructured)
                .unwrap();
    }

    public static Stream<Content> fromDeclaration(AstNode astNode) {
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
        CType type = CType.fromTypeNode(node.getFirstChild("Type"), false);
        return functionDeclaration(type).apply(node);
    }

    private static Pair<Pair<String, String>, CType> getParameter(AstNode astNode) {
        astNode = astNode.getFirstChild("var");
        boolean ptr = astNode.getFirstChild("var").toOptional().isPresent()
                || astNode.getFirstChild("identifier")
                .getFirstChild("Type")
                .getOptionalChild("ArrayType")
                .isPresent();
        AstNode identifier = astNode.getFirstChild("identifier");
        String id = identifier.getFirstChild("identifier").getContentString();
        String ptrStr = "";
        return new Pair<>(new Pair<>(id, ptrStr + id), CType.fromTypeNode(identifier.getFirstChild("Type"), ptr));
    }

    private static Function<AstNode, Stream<Content>> functionDeclaration(CType returnType) {
        return node -> {
            AstNode params = node.getFirstChild("Parameters")
                    .getFirstChild("Parameter")
                    .getFirstChild("Parameter");
            List<Pair<Pair<String, String>, CType>> collect = params.toOptional()
                    .map(p -> p.getFirstChild("Parameter").toOptional()
                            .map(CBlock::getParameter)
                            .map(m -> Stream.concat(Stream.of(m), params.getFirstChild("more").getList().stream()
                                    .map(more -> getParameter(more.getFirstChild("Parameter")))))
                            .orElse(Stream.empty())
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
            List<CType> parameters = collect.stream().map(Pair::getRight).collect(Collectors.toList());
            CType ftype = new CType(returnType, parameters);
            String identifier = node.getFirstChild("identifier").getContentString();
            IdentifierContext.addIdentifier(identifier, ftype);
            IdentifierContext.push();
            IdentifierContext.pushFunctionContext(returnType);
            collect.forEach(p -> IdentifierContext.addIdentifier(p.getLeft().getLeft(), p.getLeft().getRight(), p.getRight()));
            Stream<Content> block = Stream.of(new Content(
                    ftype.toFunctionDeclaration(
                            collect.stream().map(p -> p.getLeft().getLeft()).collect(Collectors.toList()),
                            identifier)
                            + " {\n" +
                            format(CBlock.parse(node.getFirstChild("Block")).contents.stream()) +
                            "\n}\n"));
            if (!returnType.equals(CType.CVOID) && !IdentifierContext.isLastStatementWasReturn()) {
                throw new TypeError("Last statement in function has to be a return statement");
            }
            IdentifierContext.pop();
            IdentifierContext.popFunctionContext();
            return block;
        };
    }

    private static Stream<Content> varDeclaration(AstNode astNode) {
        List<String> pre = new ArrayList<>();
        Optional<String> initializer = Optional.empty();
        String identifier = astNode.getFirstChild("identifier").getContentString();
        CType type = CType.fromTypeNode(astNode.getFirstChild("Type"), false);
        List<String> more = astNode.getFirstChild("more").toOptional()
                .map(t -> t.getList().stream()
                        .map(c -> c.getFirstChild("identifier").getContentString()))
                .orElse(Stream.empty())
                .collect(Collectors.toList());
        if (astNode.getFirstChild("Type").getOptionalChild("ArrayType").isPresent()) {
            AstNode firstChild = astNode.getFirstChild("Type").getFirstChild("ArrayType")
                    .getFirstChild("ArrayType")
                    .getFirstChild("Expression")
                    .getFirstChild("Expression");
            CExpressionResult cExpressionResult = CExpressionResult.fromExpression(firstChild);
            pre = cExpressionResult.getTemporaries();
            initializer = Optional.of(cExpressionResult.getIdentifier());
        }
        Stream.concat(Stream.of(identifier), more.stream())
                .forEach(ident -> IdentifierContext.addIdentifier(ident, type));
        Optional<String> finalInitializer = initializer;
        return Stream.concat(pre.stream().map(Content::new),
                Stream.concat(Stream.of(identifier), more.stream())
                        .map(ident -> new Content(type.toDeclaration(ident, finalInitializer) + ";")));
    }

    private static Stream<Content> fromStructured(AstNode astNode) {
        return astNode.<Stream<Content>>toMap()
                .map("Block", CBlock::fromBlock)
                .map("If", CBlock::fromIf)
                .map("While", CBlock::fromWhile)
                .unwrap();
    }

    private static Stream<Content> fromBlock(AstNode astNode) {
        IdentifierContext.push();
        List<Content> collect = Stream.concat(Stream.of(new Content("{")), Stream.concat(
                parse(astNode).getContents().stream(),
                Stream.of(new Content("}"))
        )).collect(Collectors.toList());
        IdentifierContext.pop();
        return collect.stream();
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
                .map("ReadStatement", CBlock::fromRead)
                .map("AssertStatement", CBlock::fromAssert)
                .unwrap();
    }

    private static Stream<Content> fromAssert(AstNode astNode) {
        CExpressionResult expression = CExpressionResult.fromExpression(astNode.getFirstChild("Expression"));
        return Stream.of(new Content(
                expression.getTemporaries().stream().collect(Collectors.joining(";\n"))
                        + "if(!" + expression.getIdentifier() + ") {\n" +
                        "      printf(\"Assertion failed\\n\");" +
                        "      exit(1);" +
                        "  }\n"));
    }

    private static Stream<Content> fromReturn(AstNode astNode) {
        IdentifierContext.setLastStatementWasReturn(true);
        CExpressionResult result = CExpressionResult.fromExpression(astNode.getFirstChild("Expression"));
        String p;
        p = "";
        CType type = result.getType();
        CType functionContext = IdentifierContext.getFunctionContext();
        if (type.getPtrTo().isPresent() && !functionContext.getPtrTo().isPresent()) {
            p = "*";
        }
        if (!type.equals(functionContext)) {
            throw new TypeError("Invalid return type; expected " + functionContext.formatType()
                    + " but got " + type.formatType());
        }
        return Stream.of(new Content(
                result.getTemporaries().stream().collect(Collectors.joining(";\n"))
                        + "return " + p + result.getIdentifier() + ";\n"
        ));
    }

    private static Stream<Content> fromIdentifier(AstNode identifierStatement) {
        identifierStatement = identifierStatement
                .getFirstChild("identifier");
        String identifier = identifierStatement
                .getFirstChild("identifier")
                .getContentString();
        if (identifier.toLowerCase().equals("writeln")) {
            return fromWrite(identifierStatement
                    .getFirstChild("IdentifierStatementContent")
                    .getFirstChild("Arguments"));
        }
        return identifierStatement
                .getFirstChild("IdentifierStatementContent").<Stream<Content>>toMap()
                .map("AssignmentStatement", CBlock.assignmentStatement(identifier))
                .map("Arguments", CBlock.fromCall(identifier))
                .unwrap();
    }

    private static String readVariable(String identifier) {
        CType type = IdentifierContext.getType(identifier);
        String fmtFormat = "scanf(\"" + type.toFormat() + "\", &";
        if (type.equals(CType.CSTRING)) {
            fmtFormat = "_builtin_scanstring(&";
        }
        return fmtFormat + IdentifierContext.getRealName(identifier) + ");\n";
    }

    private static Stream<Content> fromRead(AstNode firstChild) {
        AstNode variable = firstChild.getFirstChild("Variable");
        return Stream.concat(Stream.of(variable),
                firstChild.getFirstChild("more").getList().stream()
                        .map(child -> child.getFirstChild("Variable")))
                .map(m -> m.getFirstChild("identifier"))
                .map(AstNode::getContentString)
                .map(CBlock::readVariable)
                .map(Content::new)
                .collect(Collectors.toList())
                .stream();
    }

    private static Function<AstNode, Stream<Content>> assignmentStatement(String identifier) {
        return assignmentStatement -> assignmentStatement
                .getFirstChild("ob")
                .getFirstChild("ob")
                .toOptional()
                .map(ob -> {
                    CExpressionResult index = CExpressionResult.fromExpression(ob.getFirstChild("Expression"));
                    return Stream.concat(index.getTemporaries().stream().map(Content::new),
                            assignTo(identifier + "[" + index.getIdentifier() + "]",
                                    assignmentStatement,
                                    false)
                    );
                })
                .orElseGet(() -> assignTo(
                        identifier, assignmentStatement, true));
    }

    private static Stream<Content> assignTo(String identifier, AstNode exp, boolean checkLeftPtr) {
        exp = exp.getFirstChild("ob").getFirstChild("assign").getFirstChild("Expression");
        CExpressionResult expression = CExpressionResult.fromExpression(exp);
        String p1 = checkLeftPtr && IdentifierContext.getType(identifier).getPtrTo().isPresent()
                ? "*" : "";
        String realIdentifier =
                checkLeftPtr
                        ? IdentifierContext.getRealName(identifier)
                        : identifier;
        String p2 = expression.getType().getPtrTo().map($ -> "*").orElse("");
        return Stream.concat(expression.getTemporaries().stream(),
                Stream.of(p1 + realIdentifier + " = " + p2 + expression.getIdentifier() + ";"))
                .map(Content::new);
    }

    private static Stream<Content> fromWrite(AstNode writeNode) {
        List<CExpressionResult> collect = getArguments(writeNode.getFirstChild("Arguments"));

        String fmt = collect.stream()
                .map(result -> result.getType().toFormat() + " ")
                .collect(Collectors.joining());

        String print = "printf(\"" + fmt.trim() + "\\n\", " + collect.stream()
                .map(cExpressionResult ->
                        (cExpressionResult.getType().getPtrTo().isPresent() ? "*" : "")
                                + cExpressionResult.getIdentifier())
                .collect(Collectors.joining(", ")) + ");\n";
        String total = formatExpressions(collect, $ -> print);
        return Stream.of(new Content(total));
    }

    private static Function<AstNode, Stream<Content>> fromCall(String identifier) {
        return argumentsNode -> {
            List<CExpressionResult> collect = getArguments(argumentsNode);

            String pre = identifier + "(";
            String delimit = ", ";
            String post = ");\n";

            String total = formatExpressions(
                    collect,
                    expressions -> Streams.zip(expressions.stream(), IdentifierContext.getType(identifier).getParameters().stream(),
                            (a, b) -> a.getType().assignTo(b, a.getIdentifier()))
                            .collect(Collectors.joining(delimit, pre, post)));

            return Stream.of(new Content(total));
        };
    }

    private static String format(Stream<Content> blocks) {
        return blocks
                .map(c -> c.data)
                .collect(Collectors.joining());
    }
}
