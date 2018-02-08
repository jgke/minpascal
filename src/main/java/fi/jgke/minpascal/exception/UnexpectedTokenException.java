package fi.jgke.minpascal.exception;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;

import java.util.List;

public class UnexpectedTokenException extends UserError {
    public UnexpectedTokenException(Token<?> token, TokenType type) {
        super("Unexpected token: " + token.getType() + "[" + token.getValue() +
                "], expected " + type + " near " + token.getPosition());
    }

    public UnexpectedTokenException(Token<?> token, List<TokenType> types) {
        super("Unexpected token: " + token.getType() + "[" + token.getValue() +
                "], expected any of: " + types + " near " + token.getPosition());
    }
}
