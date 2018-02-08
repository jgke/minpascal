package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.compiler.IdentifierContext;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.nodes.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;
import java.util.function.Function;

import static fi.jgke.minpascal.compiler.IdentifierContext.genIdentifier;

@Data
@AllArgsConstructor
public class CExpressionResult {
    private final CType type;
    private final String identifier;
    private final String temporaries;
    private final String post;

    public static CExpressionResult fromExpression(ExpressionNode arg) {
        return getType(arg);
    }

    private static <T> CExpressionResult getType(T left, Optional<Token> operator, Optional<T> right, Function<T, CExpressionResult> get) {
        //noinspection ConstantConditions
        return operator.map(
                op -> CBinaryExpressions.apply(get.apply(left), op, get.apply(right.get()))
        ).orElse(get.apply(left));
    }

    private static CExpressionResult getType(ExpressionNode arg) {
        return getType(arg.getLeft(), arg.getOperator(), arg.getRight(), CExpressionResult::getType);
    }

    private static CExpressionResult getType(SimpleExpressionNode arg) {
        return getType(arg.getLeft(), arg.getAddingOperator(), arg.getRight(), CExpressionResult::getType);
    }

    private static CExpressionResult getType(TermNode arg) {
        return getType(arg.getLeft(), arg.getOperator(), arg.getRight(), CExpressionResult::getType);
    }

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
        String identifier = var.getIdentifier().getValue().get().toString();
        CType type = IdentifierContext.getType(identifier);
        var.getArrayAccessInteger().ifPresent($ -> notImplemented());
        return new CExpressionResult(type, identifier, "", ":w");
    }

    private static CExpressionResult toExpression(CallNode call) {
        return notImplemented();
    }

    private static CExpressionResult notImplemented() {
        throw new CompilerException("Not implemented");
    }

    private static CExpressionResult toExpression(CType type, Object value) {
        String id = genIdentifier();
        return new CExpressionResult(type, id, type.toDeclaration(id) + " = " + value + ";", "");
    }

    private static CExpressionResult getLiteralType(LiteralNode literalNode) {
        return literalNode.map(
                i -> toExpression(CType.CINTEGER, i),
                d -> toExpression(CType.CDOUBLE, d),
                s -> toExpression(CType.CSTRING, '"' + s + '"'),
                b -> toExpression(CType.CBOOLEAN, b)
        );
    }
}
