package fi.jgke.minpascal.parser.base;

import fi.jgke.minpascal.data.TokenType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static fi.jgke.minpascal.data.TokenType.*;

public class ParseUtils {
    public static final TokenType[] relationalOperators = new TokenType[]{
            EQUALS, NOTEQUALS, LESSTHAN, LESSTHANEQUALS, MORETHAN, MORETHANEQUALS
    };

    public static final TokenType[] multiplyingOperators = new TokenType[]{
            TIMES, DIVIDE, MOD, AND
    };

    public static final TokenType[] addingOperators = new TokenType[]{
            PLUS, MINUS, OR
    };

    public static List<TokenType> collectTokenTypes(Parsable... parsables) {
        return Arrays.stream(parsables)
                .flatMap(token -> token.getMatchableTokens().stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
