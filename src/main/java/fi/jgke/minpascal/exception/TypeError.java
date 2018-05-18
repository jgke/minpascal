package fi.jgke.minpascal.exception;

import fi.jgke.minpascal.data.Position;

public class TypeError extends UserError {
    public TypeError(String message, Position position) {
        super(String.format("Type error: %s", message), position);
    }
}
