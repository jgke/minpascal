package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.compiler.IdentifierContext;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.exception.TypeError;
import fi.jgke.minpascal.parser.nodes.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.jgke.minpascal.compiler.IdentifierContext.genIdentifier;

@Data
@AllArgsConstructor
public class CExpressionResult {
    private final CType type;
    private final String identifier;
    private final List<String> temporaries;
    private final List<String> post;

    public static CExpressionResult fromExpression(ExpressionNode arg) {
        return getType(arg);
    }

    private static <T> CExpressionResult getType(T left, Optional<Token<Void>> operator, Optional<T> right, Function<T, CExpressionResult> get) {
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
        String identifier = var.getIdentifier().getValue();
        CType type = IdentifierContext.getType(identifier);
        var.getArrayAccessInteger().ifPresent($ -> notImplemented());
        return new CExpressionResult(type, identifier, Collections.emptyList(), Collections.emptyList());
    }

    private static CExpressionResult toExpression(CallNode call) {
        String fn = call.getIdentifier().getValue();
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
        CType type = IdentifierContext.getType(fn).getCall()
                .orElseThrow(() -> new TypeError(call.getIdentifier().getPosition(), "Identifier " + fn + " is not a function"));
        temporaries.add(type.toDeclaration(result) + " = " + fn + "(" + arguments + ");");
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

    private static CExpressionResult getLiteralType(LiteralNode literalNode) {
        return literalNode.map(
                i -> toExpression(CType.CINTEGER, i),
                d -> toExpression(CType.CDOUBLE, d),
                s -> toExpression(CType.CSTRING, '"' + s + '"'),
                b -> toExpression(CType.CBOOLEAN, b)
        );
    }
}
