package fi.jgke.minpascal.exception;

import fi.jgke.minpascal.data.Position;

public class ParseException extends UserError {
    public ParseException(Position position, String message) {
        super(String.format("Parse exception at %s: %s", position, message));
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String s, Throwable e) {
        super(s, e);
    }
}
