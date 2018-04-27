package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.LeafNode;
import fi.jgke.minpascal.compiler.CType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Data
@AllArgsConstructor
public class CExpressionResult {
    private final CType type;
    private final String identifier;
    private final List<String> temporaries;
    private final List<String> post;

    public static CExpressionResult fromExpression(AstNode arg) {
        throw new RuntimeException("not impl");
    }

    private static <T> CExpressionResult getType(T left, Optional<LeafNode> operator, Optional<T> right, Function<T, CExpressionResult> get) {
        //noinspection ConstantConditions
        return operator.map(
                op -> CBinaryExpressions.apply(get.apply(left),
                        CBinaryExpressionFnPrivate.getOperator(op.getContent().toString()),
                        get.apply(right.get()))
        ).orElse(get.apply(left));
    }

    /*
    private static CExpressionResult getType(FactorNode factor) {
        return factor.map(
                CExpressionResult::toExpression,
                CExpressionResult::toExpression,
                CExpressionResult::getType,
                not -> CExpressionResult.getType(not.getFactor()),
                CExpressionResult::getLiteralType,
                sizeNode -> notImplemented()
        );
    }

    private static CExpressionResult toExpression(VariableNode var) {
        String identifier = var.getIdentifier().getValue();
        CType type = IdentifierContext.getType(identifier);
        var.getArrayAccessInteger().ifPresent($ -> notImplemented());
        return new CExpressionResult(type, identifier, Collections.emptyList(), Collections.emptyList());
    }

    private static CExpressionResult fromCall(String identifier, AstNode call) {
        List<CExpressionResult> expressions = call.getArguments().getArguments().stream()
                .map(CExpressionResult::fromExpression)
                .collect(Collectors.toList());
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
        CType type = IdentifierContext.getType(identifier).getCall()
                .orElseThrow(() -> new TypeError(call.getIdentifier().getPosition(), "Identifier " + identifier + " is not a function"));
        temporaries.add(type.toDeclaration(result) + " = " + identifier + "(" + arguments + ");");
        return new CExpressionResult(type, result, temporaries, post);
    }

    private static CExpressionResult notImplemented() {
        throw new CompilerException("Not implemented");
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
                .map("stringliteral", d -> toExpression(CType.CSTRING, '"' + d.getContentString() + '"'))
                .map("true", t -> toExpression(CType.CBOOLEAN, "true"))
                .map("false", t -> toExpression(CType.CBOOLEAN, "false"))
                .unwrap();
    }
    */
}
