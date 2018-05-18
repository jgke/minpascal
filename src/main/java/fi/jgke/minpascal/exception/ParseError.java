package fi.jgke.minpascal.exception;

import fi.jgke.minpascal.data.Position;

import static fi.jgke.minpascal.util.StringUtils.ellipsis;

public class ParseError extends UserError {
    public ParseError(String pattern, String finalStr, Position position) {
        super("Parse error: could not match '" + pattern
                + "' for string '" +
                ellipsis(finalStr.replace("\n", " "), 40)
                + "'", position);
    }

    public ParseError(String msg, Position right) {
        super("Parse error: " + msg, right);
    }
}
