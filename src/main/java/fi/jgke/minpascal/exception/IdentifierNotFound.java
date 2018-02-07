package fi.jgke.minpascal.exception;

public class IdentifierNotFound extends UserError {
    public IdentifierNotFound(String identifier) {
        super("Identifier " + identifier + " not found");
    }
}
