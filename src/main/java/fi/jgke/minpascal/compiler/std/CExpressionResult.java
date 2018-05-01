package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.LeafNode;
import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.exception.CompilerException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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
        left.debug();
        return relOp.toOptional().map(rel ->
                getType(fromSimple(left),
                        rel.getFirstChild("RelOp"),
                        fromSimple(rel.getFirstChild("SimpleExpression")))
        ).orElse(fromSimple(left));
    }

    private static CExpressionResult fromSimple(AstNode simple) {
        simple = simple.getFirstChild("Sign");
        Optional<String> sign = simple.getFirstChild("Sign")
                .toOptional()
                .map(CExpressionResult::getSign);
        AstNode left = simple.getFirstChild("Term");
        left.debug();
        CExpressionResult result = left.getFirstChild("AddOp").toOptional()
                .map(add -> getType(fromTerm(left),
                        add.getFirstChild("AddOp"),
                        fromTerm(add.getFirstChild("Term"))
                )).orElse(fromTerm(left));
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

    private static CExpressionResult fromTerm(AstNode term) {
        AstNode left = term.getFirstChild("Term");
        AstNode relOp = term.getFirstChild("AddOp");
        return relOp.toOptional().map(rel ->
                getType(fromFactor(left),
                        rel.getFirstChild("MulOp"),
                        fromFactor(rel.getFirstChild("Factor")))
        ).orElse(fromFactor(left));
    }

    private static CExpressionResult fromFactor(AstNode term) {
        AstNode factor = term.getFirstChild("Factor");
        AstNode subFactor = factor.getFirstChild("SubFactor");
        return subFactor.<CExpressionResult>toMap()
                .map("Variable", notImplemented())
                .map("Literal", CExpressionResult::getLiteralType)
                .map("op", notImplemented())
                .map("not", notImplemented())
                .unwrap();
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
    */

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
}
