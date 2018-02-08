package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.data.Token;

import static fi.jgke.minpascal.compiler.IdentifierContext.genIdentifier;

@FunctionalInterface
public interface CBinaryExpressionFn {
    CExpressionResult apply(CExpressionResult left, Token op, CExpressionResult right);

    static CBinaryExpressionFn std(CType type) {
        return (left, op, right) -> {
            String tmp = left.getTemporaries() + right.getTemporaries();
            String combined = left.getIdentifier() + " " +
                    CBinaryExpressionFnPrivate.getOperator(op.getType()) + " " +
                    right.getIdentifier() + ";";
            String newId = genIdentifier();
            String bufs = left.getPost() + right.getPost();
            return new CExpressionResult(type, newId,
                    tmp + type.toDeclaration(newId) + " = " + combined,
                    bufs);
        };
    }

    static CBinaryExpressionFn str(CType type) {
        return (left, op, right) -> {
            String template = "" +
                    "size_t %s = snprintf(NULL, 0, %s, %s, %s);" +
                    "char *%s = malloc(%s);" +
                    "snprintf(%s, %s, %s, %s, %s);";
            String tmp = left.getTemporaries() + right.getTemporaries();
            String sizeId = genIdentifier();
            String resultId = genIdentifier();
            String format = '"' + left.getType().toFormat() + right.getType().toFormat() + '"';
            tmp += String.format(template,
                    sizeId, format, left.getIdentifier(), right.getIdentifier(),
                    resultId, sizeId,
                    resultId, sizeId, format, left.getIdentifier(), right.getIdentifier()
            );
            String bufs = left.getPost() + right.getPost() + "free(" + resultId + ");";
            return new CExpressionResult(type, resultId, tmp, bufs);
        };
    }
}
