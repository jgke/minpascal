package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.TokenType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParseUtils {
    public static List<TokenType> collectTokenTypes(Parsable... parsables) {
        return Arrays.stream(parsables)
                .flatMap(token -> token.getMatchableTokens().stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
