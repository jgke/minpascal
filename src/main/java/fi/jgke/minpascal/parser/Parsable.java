package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.data.TreeNode;

import java.util.List;

public interface Parsable {
    /**
     * Does this Parsable match the ParseQueue?
     */
    boolean matches(ParseQueue queue);

    /**
     * Get a list of tokens which could have been matched, for error messages.
     */
    List<TokenType> getMatchableTokens();

    /**
     * Parse the ParseQueue for this Parsable.
     */
    TreeNode parse(ParseQueue queue);
}
