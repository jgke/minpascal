package fi.jgke.minpascal.exception;

public class CompilerException extends RuntimeException {
    public CompilerException(String message) {
        super("Unexpected state reached: " + message + " - this is a compiler bug");
    }

    public CompilerException(Throwable throwable) {
        super(throwable);
    }
}
