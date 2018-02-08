package fi.jgke.minpascal.exception;

import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.data.Token;

public class OperatorError extends UserError {
    public OperatorError(CType left, Token<Void> operator, CType right) {
        super(String.format("Cannot use operator %s for types %s and %s near %s",
                operator.getType(), left, right, operator.getPosition()));
    }
}
