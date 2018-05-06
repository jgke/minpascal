package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.compiler.IdentifierContext;
import fi.jgke.minpascal.exception.CompilerException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jgke.minpascal.compiler.IdentifierContext.genIdentifier;

@Data
@AllArgsConstructor
public class CExpressionResult {
    private final CType type;
    private String identifier;
    private final List<String> temporaries;
    private final List<String> post;

    public static CExpressionResult fromExpression(AstNode arg) {
        AstNode left = arg.getFirstChild("SimpleExpression");
        AstNode relOp = arg.getFirstChild("RelOp");
        return relOp.toOptional().map(rel ->
                getType(fromSimple(left),
                        rel.getFirstChild("RelOp").getFirstChild("RelOp"),
                        fromSimple(rel.getFirstChild("RelOp").getFirstChild("SimpleExpression")))
        ).orElse(fromSimple(left));
    }

    private static CExpressionResult fromSimple(AstNode simple) {
        simple = simple.getFirstChild("Sign");
        Optional<String> sign = simple.getFirstChild("Sign")
                .toOptional()
                .map(CExpressionResult::getSign);
        AstNode left = simple.getFirstChild("Term");
        CExpressionResult result = left.getFirstChild("AddOp").toOptional()
                .map(add -> getType(fromTerm(left.getFirstChild("Term")),
                        add.getFirstChild("AddOp"),
                        fromTerm(add.getFirstChild("AddOp").getFirstChild("Term"))
                )).orElse(fromTerm(left.getFirstChild("Term")));
        sign.ifPresent(addSign(result));
        return result;
    }

    private static CExpressionResult getType(CExpressionResult left, AstNode op, CExpressionResult right) {
        return null;
    }

    private static Consumer<String> addSign(CExpressionResult result) {
        return s -> {
            CType type = result.getType();
            if (!type.equals(CType.CINTEGER) &&
                    !type.equals(CType.CDOUBLE))
                throw new CompilerException("Type error");
            String id = genIdentifier();
            result.temporaries.add(type.toDeclaration(id) + " = " + s + result.getIdentifier());
            result.identifier = id;
        };
    }

    private static CExpressionResult fromTerm(AstNode left) {
        AstNode relOp = left.getFirstChild("MulOp");
        return relOp.toOptional().map(rel ->
                getType(fromFactor(left.getFirstChild("Factor")),
                        rel.getFirstChild("MulOp"),
                        fromFactor(rel.getFirstChild("MulOp").getFirstChild("Factor")))
        ).orElse(fromFactor(left.getFirstChild("Factor")));
    }

    private static CExpressionResult fromFactor(AstNode factor) {
        AstNode subFactor = factor.getFirstChild("SubFactor");
        return subFactor.<CExpressionResult>toMap()
                .map("Variable", CExpressionResult::fromVariable)
                .map("Literal", CExpressionResult::getLiteralType)
                .map("op", notImplemented())
                .map("not", notImplemented())
                .unwrap();
    }

    private static CExpressionResult fromVariable(AstNode astNode) {
        String identifier = astNode
                .getFirstChild("Variable")
                .getFirstChild("identifier").getContentString();
        return astNode.getFirstChild("Arguments").toOptional()
                .map(args -> fromCall(identifier, args))
                .orElseGet(() -> new CExpressionResult(IdentifierContext.getType(identifier),
                        identifier,
                        Collections.emptyList(),
                        Collections.emptyList()));
    }

    private static Function<AstNode, CExpressionResult> notImplemented() {
        return $ -> {
            throw new RuntimeException();
        };
    }

    private static String getSign(AstNode sign) {
        return sign.<String>toMap()
                .map("plus", $ -> "+")
                .map("minus", $ -> "-")
                .unwrap();
    }

    private static CExpressionResult fromCall(String identifier, AstNode call) {
        List<CExpressionResult> expressions = CExpressionResult.getArguments(call);
        List<String> temporaries = expressions.stream()
                .flatMap(e -> e.getTemporaries().stream())
                .collect(Collectors.toList());
        List<String> post = expressions.stream()
                .flatMap(e -> e.getPost().stream())
                .collect(Collectors.toList());
        String arguments = expressions.stream()
                .map(CExpressionResult::getIdentifier)
                .collect(Collectors.joining(", "));
        String result = genIdentifier();
        CType type = IdentifierContext.getType(identifier).getReturnType()
                .orElseThrow(() -> new CompilerException("Identifier not found"));
        temporaries.add(type.toDeclaration(result) + " = " + identifier + "(" + arguments + ");");
        return new CExpressionResult(type, result, temporaries, post);
    }

    private static CExpressionResult toExpression(CType type, Object value) {
        String id = genIdentifier();
        return new CExpressionResult(type, id, Collections.singletonList(type.toDeclaration(id) + " = " + value + ";"),
                Collections.emptyList());
    }

    private static CExpressionResult getLiteralType(AstNode literalNode) {
        return literalNode.<CExpressionResult>toMap()
                .map("realliteral", d -> toExpression(CType.CDOUBLE, Double.parseDouble(d.getContentString())))
                .map("integerliteral", d -> toExpression(CType.CINTEGER, Integer.parseInt(d.getContentString())))
                .map("stringliteral", d -> toExpression(CType.CSTRING, d.getContentString()))
                .unwrap();
    }

    public static String formatExpressions(List<CExpressionResult> expressions,
                                           Function<List<CExpressionResult>, Object> core) {
        List<List<String>> steps = new ArrayList<>();
        List<String> post = new ArrayList<>();

        for (CExpressionResult result : expressions) {
            steps.add(result.getTemporaries());
            post.addAll(result.getPost());
        }

        String pre = steps.stream()
                .map(list -> list.stream()
                        .collect(Collectors.joining("\n")) + "\n")
                .collect(Collectors.joining("\n")) + "\n";
        String clean = post.stream().collect(Collectors.joining("\n")) + "\n";
        return pre + core.apply(expressions) + clean;
    }

    public static List<CExpressionResult> getArguments(AstNode argumentsNode) {
        return argumentsNode
                .getFirstChild("Arguments")
                .getFirstChild("Expression")
                .toOptional()
                .map(node -> node.getFirstChild("Expression"))
                .map(node -> Stream.concat(Stream.of(node.getFirstChild("Expression")),
                        node.getFirstChild("more").getList().stream())
                ).orElse(Stream.empty())
                .map(CExpressionResult::fromExpression)
                .collect(Collectors.toList());
    }
}
