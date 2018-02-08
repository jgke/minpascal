package fi.jgke.minpascal.data;

import fi.jgke.minpascal.exception.CompilerException;
import lombok.Data;
import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Data
public class Token<T> {
    @NonNull private final TokenType type;
    @NonNull private final T value;
    @NonNull private final Position position;

    private Token(TokenType type, T value, Position position) {
        if (!type.acceptedType().isInstance(value)) {
            throw new CompilerException(
                    "Invalid value, expected " + type.acceptedType() +
                    " but got " + value.getClass());
        }
        this.type = type;
        this.value = value;
        this.position = position;
    }

    public static <T> Token<T> token(TokenType type, T value, Position position) {
        return new Token<>(type, value, position);
    }

    public static Token<Void> token(TokenType type, Position position) {
        final Constructor<?> c = Void.class.getDeclaredConstructors()[0];
        c.setAccessible(true);
        try {
            return new Token<>(type, (Void) c.newInstance(), position);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new CompilerException("Did evil things and failed :(");
        }
    }
}
