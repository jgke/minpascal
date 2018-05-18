package fi.jgke.minpascal.exception;

import fi.jgke.minpascal.data.Position;

public class IdentifierNotFound extends UserError {
    public IdentifierNotFound(String identifier, Position position) {
        super("Identifier " + identifier + " not found", position);
    }
}
