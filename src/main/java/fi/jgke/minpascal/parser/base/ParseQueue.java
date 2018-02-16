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
import java.util.function.Predicate;

import static fi.jgke.minpascal.parser.base.ParseUtils.collectTokenTypes;

public final class ParseQueue extends ArrayDeque<Token<?>> {
    public Token<?> getExpectedToken(TokenType... types) {
        Token<?> token = this.remove();
        return Arrays.stream(types)
                .filter(t -> t.matches(token))
                .findFirst()
                .map(ignored -> token) // Workaround an IDE warning :)
                .orElseThrow(() ->
                        types.length == 1
                        ? new UnexpectedTokenException(token, types[0])
                        : new UnexpectedTokenException(token, Arrays.asList(types)));
    }

    public Token<String> getIdentifier() {
        //noinspection unchecked
        return (Token<String>) getExpectedToken(TokenType.IDENTIFIER);
    }

    public void getExpectedTokens(TokenType... types) {
        Arrays.stream(types)
                .forEach(this::getExpectedToken);
    }

    public boolean isNext(TokenType... types) {
        return Arrays.stream(types).anyMatch(type -> type.matches(this));
    }

    public boolean ifNextConsume(TokenType... types) {
        if (!isNext(types)) {
            return false;
        }
        getExpectedToken(types);
        return true;
    }

    public TreeNode any(Parsable... parsables) {
        Token<?> next = this.peek();
        return Arrays.stream(parsables)
                .filter(parsable -> parsable.matches(this))
                .findFirst()
                .orElseThrow(() -> {
                    List<TokenType> tokenTypes = collectTokenTypes(parsables);
                    if (tokenTypes.size() == 1) {
                        return new UnexpectedTokenException(next, tokenTypes.get(0));
                    }
                    return new UnexpectedTokenException(next, tokenTypes);
                })
                .parse(this);
    }

    public boolean anyMatches(Parsable... parsables) {
        return Arrays.stream(parsables)
                .anyMatch(parsable -> parsable.matches(this));
    }

    public <T> List<T> collectBy(Function<ParseQueue, T> provider, boolean requireOne,
                                 TokenType delimiter, Predicate<ParseQueue> matches,
                                 boolean ignoreParseError) {
        List<T> results = new ArrayList<>();
        if (!requireOne && !matches.test(this)) {
            return results;
        }

        do {
            results.add(provider.apply(this));
        } while (ifNextConsume(delimiter) && (!ignoreParseError || matches.test(this)));

        return results;
    }

    public <T> List<T> collectBy(Function<ParseQueue, T> provider, TokenType delimiter) {
        return collectBy(provider, false, delimiter, queue -> !queue.isNext(TokenType.CLOSEPAREN), false);
    }

    public <T> List<T> collectBy(Function<ParseQueue, T> provider, TokenType delimiter, boolean requireOne) {
        return collectBy(provider, requireOne, delimiter, queue -> !queue.isNext(TokenType.CLOSEPAREN), false);
    }
}
