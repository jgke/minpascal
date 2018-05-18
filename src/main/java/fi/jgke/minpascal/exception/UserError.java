package fi.jgke.minpascal.exception;

import fi.jgke.minpascal.data.Position;

public class UserError extends RuntimeException {
    protected UserError(String s, Position position) {
        super("Error near " + position + ": " + s);
    }
}
