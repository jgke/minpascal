package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.compiler.IdentifierContext;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.exception.TypeError;
import fi.jgke.minpascal.parser.nodes.*;
import lombok.Data;

import java.util.Optional;
import java.util.function.Function;

@Data
public class CExpressionResult {
    private final CType type;
    private final String identifier;

    public static CExpressionResult fromExpression(ExpressionNode arg) {
        return new CExpressionResult(getType(arg), "");
    }

    private static CType getType(ExpressionNode arg) {
        SimpleExpressionNode left = arg.getLeft();
        CType type1 = getType(left);
        return arg.getOperator().map(op -> {
            assertTypeEquals(left, arg.getRight(), op.getPosition(), CExpressionResult::getType);
            return new CType("bool");
        }).orElse(type1);
    }

    private static CType getType(SimpleExpressionNode arg) {
        TermNode left = arg.getLeft();
        CType type = getType(left);
        return arg.getAddingOperator().map(op -> {
            assertTypeEquals(left, arg.getRight(), op.getPosition(), CExpressionResult::getType);
            if (op.getType().equals(TokenType.OR) && !type.equals(CType.BOOLEAN)) {
                throw new TypeError(op.getPosition(), CType.BOOLEAN, type);
            } /* check more */
            return type;
        }).orElse(type);
    }

    private static CType getType(TermNode arg) {
        FactorNode left = arg.getLeft();
        CType type = getType(left);
        return arg.getOperator().map(op -> {
            assertTypeEquals(left, arg.getRight(), op.getPosition(), CExpressionResult::getType);
            if (op.getType().equals(TokenType.AND) && !type.equals(CType.BOOLEAN)) {
                throw new TypeError(op.getPosition(), CType.BOOLEAN, type);
            } /* check more */
            return type;
        }).orElse(getType(left));
    }

    private static CType getType(FactorNode factor) {
        return factor.map(
                call -> IdentifierContext.getType(call.getIdentifier().getValue().get().toString()),
                var -> IdentifierContext.getType(var.getIdentifier().getValue().get().toString()),
                CExpressionResult::getType,
                not -> CExpressionResult.getType(not.getFactor()),
                CExpressionResult::getLiteralType,
                $ -> CType.INTEGER
        );
    }

    private static CType getLiteralType(LiteralNode literalNode) {
        return literalNode.map(
                $ -> CType.INTEGER,
                $ -> CType.DOUBLE,
                $ -> CType.STRING,
                $ -> CType.BOOLEAN
        );
    }

    private static <T> void assertTypeEquals(T a, Optional<T> b, Position p, Function<T, CType> fn) {
        assert b.isPresent();
        if (fn.apply(a).equals(fn.apply(b.get()))) {
            throw new TypeError(p, fn.apply(a), fn.apply(b.get()));
        }
    }

}
