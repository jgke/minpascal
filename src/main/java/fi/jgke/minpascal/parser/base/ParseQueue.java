package fi.jgke.minpascal.parser.base;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.exception.UnexpectedTokenException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static fi.jgke.minpascal.parser.base.ParseUtils.collectTokenTypes;

public final class ParseQueue extends ArrayDeque<Token> {
    public Token getExpectedToken(TokenType... types) {
        Token token = this.remove();
        TokenType type = token.getType();
        return Arrays.stream(types)
                .filter(type::equals)
                .findFirst()
                .map(ignored -> token) // Workaround an IDE warning :)
                .orElseThrow(() -> new UnexpectedTokenException(token, Arrays.asList(types)));
    }

    public void getExpectedTokens(TokenType... types) {
        Arrays.stream(types)
                .forEach(this::getExpectedToken);
    }

    public boolean isNext(TokenType... types) {
        return Arrays.stream(types).anyMatch(this.peek().getType()::equals);
    }

    public boolean ifNextConsume(TokenType... types) {
        if (!isNext(types)) {
            return false;
        }
        getExpectedToken(types);
        return true;
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

    public <T> List<T> collectByComma(Function<ParseQueue, T> provider, boolean requireOne) {
        List<T> results = new ArrayList<>();
        if (!requireOne && this.isNext(TokenType.CLOSEPAREN)) {
            return results;
        }

        do {
            results.add(provider.apply(this));
        } while (ifNextConsume(TokenType.COMMA));

        return results;
    }

    public <T> List<T> collectByComma(Function<ParseQueue, T> provider) {
        return collectByComma(provider, false);
    }
}
