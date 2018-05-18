package fi.jgke.minpascal.exception;

import fi.jgke.minpascal.data.Position;

public class IdentifierAlreadyExists extends UserError {
    public IdentifierAlreadyExists(String identifier, Position position, Position originalPosition) {
        super("Identifier " + identifier + " already exists, original defined at " + originalPosition, position);
    }
}
