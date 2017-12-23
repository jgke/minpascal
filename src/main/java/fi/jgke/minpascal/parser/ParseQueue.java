package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.exception.UnexpectedTokenException;

import java.util.ArrayDeque;
import java.util.Arrays;

import static fi.jgke.minpascal.parser.ParseUtils.collectTokenTypes;

public final class ParseQueue extends ArrayDeque<Token> {
    public Token getExpectedToken(TokenType type) {
        Token token = this.remove();
        if (token.getType().equals(type))
            return token;
        throw new UnexpectedTokenException(token, type);
    }

    public void getExpectedTokens(TokenType... types) {
        Arrays.stream(types)
                .forEach(this::getExpectedToken);
    }

    public boolean isNext(TokenType... types) {
        return Arrays.stream(types).anyMatch(this.peek().getType()::equals);
    }

    public TreeNode any(Parsable... parsables) {
        Token next = this.peek();
        return Arrays.stream(parsables)
                .filter(parsable -> parsable.matches(this))
                .findFirst()
                .orElseThrow(() -> new UnexpectedTokenException(next, collectTokenTypes(parsables)))
                .parse(this);
    }

    public boolean anyMatches(Parsable... parsables) {
        return Arrays.stream(parsables)
                .anyMatch(parsable -> parsable.matches(this));
    }
}
