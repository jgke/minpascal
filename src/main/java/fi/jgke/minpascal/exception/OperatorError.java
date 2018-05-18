package fi.jgke.minpascal.exception;

import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.data.Position;

public class OperatorError extends UserError {
    public OperatorError(CType left, String operator, CType right, Position position) {
        super(String.format("Cannot use operator %s for types %s and %s",
                operator, left.formatType(), right.formatType()), position);
    }
}
