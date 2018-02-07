package fi.jgke.minpascal.exception;

public class IdentifierAlreadyExists extends UserError {
    public IdentifierAlreadyExists(String identifier) {
        super("Identifier " + identifier + " already exists");
    }
}
