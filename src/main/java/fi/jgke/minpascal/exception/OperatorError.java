package fi.jgke.minpascal.exception;

import fi.jgke.minpascal.compiler.CType;

public class OperatorError extends UserError {
    public OperatorError(CType left, String operator, CType right) {
        super(String.format("Cannot use operator %s for types %s and %s",
                operator, left.formatType(), right.formatType()));
    }
}
