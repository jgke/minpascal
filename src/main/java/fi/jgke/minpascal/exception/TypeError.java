package fi.jgke.minpascal.exception;

import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.data.Position;

public class TypeError extends UserError {
    public TypeError(Position position, CType left, CType right) {
        super(String.format("Type error: %s is not equal to %s near %s", left, right, position));
    }
}