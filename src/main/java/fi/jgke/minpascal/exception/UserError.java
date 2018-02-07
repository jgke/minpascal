package fi.jgke.minpascal.exception;

public class UserError extends RuntimeException {
    UserError(String s) {
        super(s);
    }

    UserError(String s, Throwable throwable) {
        super(s, throwable);
    }
}
