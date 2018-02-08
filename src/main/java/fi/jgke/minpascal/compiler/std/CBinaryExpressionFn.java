package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.data.Token;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jgke.minpascal.compiler.IdentifierContext.genIdentifier;

@FunctionalInterface
public interface CBinaryExpressionFn {
    CExpressionResult apply(CExpressionResult left, Token<Void> op, CExpressionResult right);

    static CBinaryExpressionFn std(CType type) {
        return (left, op, right) -> {
            String combined = left.getIdentifier() + " " +
                    CBinaryExpressionFnPrivate.getOperator(op.getType()) + " " +
                    right.getIdentifier() + ";";
            String newId = genIdentifier();
            List<String> tmp = Stream.of(
                    left.getTemporaries(),
                    right.getTemporaries(),
                    Collections.singletonList(type.toDeclaration(newId) + " = " + combined)
            ).flatMap(List::stream).collect(Collectors.toList());
            return new CExpressionResult(type, newId, tmp,
                    Stream.of(left.getPost(), right.getPost())
                            .flatMap(List::stream)
                            .collect(Collectors.toList())
            );
        };
    }

    static CBinaryExpressionFn str(CType type) {
        return (left, op, right) -> {
            String template = "" +
                    "size_t %s = snprintf(NULL, 0, %s, %s, %s);\n" +
                    "char *%s = malloc(%s);\n" +
                    "snprintf(%s, %s, %s, %s, %s);";
            String sizeId = genIdentifier();
            String resultId = genIdentifier();
            String format = '"' + left.getType().toFormat() + right.getType().toFormat() + '"';
            List<String> tmp = Stream.of(
                    left.getTemporaries(),
                    right.getTemporaries(),
                    Collections.singletonList(String.format(template,
                            sizeId, format, left.getIdentifier(), right.getIdentifier(),
                            resultId, sizeId,
                            resultId, sizeId, format, left.getIdentifier(), right.getIdentifier()
                    ))).flatMap(List::stream).collect(Collectors.toList());
            return new CExpressionResult(type, resultId, tmp,
                    Stream.of(left.getPost(), right.getPost(), Collections.singletonList("free(" + resultId + ");"))
                            .flatMap(List::stream)
                            .collect(Collectors.toList())
            );
        };
    }
}
