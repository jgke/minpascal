package fi.jgke.minpascal.parser.base;

import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.data.TreeNode;

import java.util.List;

public interface Parsable {
    /**
     * Does this Parsable match the ParseQueue?
     */
    default boolean matches(ParseQueue queue) {
        return getParsables().stream()
                .anyMatch(parsable -> parsable.matches(queue));
    }

    /**
     * Get a list of tokens which could have been matched, for error messages.
     */
    default List<TokenType> getMatchableTokens() {
        return ParseUtils.collectTokenTypes(getParsables().toArray(new Parsable[0]));
    }

    /**
     * Get a list of parsables which can be matched.
     */
    List<Parsable> getParsables();

    /**
     * Parse the ParseQueue for this Parsable.
     */
    default TreeNode parse(ParseQueue queue) {
        return queue.any(getParsables().toArray(new Parsable[0]));
    }
}
