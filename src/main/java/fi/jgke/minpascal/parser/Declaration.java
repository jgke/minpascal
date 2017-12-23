package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.data.TreeNode;

import java.util.List;

public class Declaration implements Parsable {
    @Override
    public boolean matches(ParseQueue queue) {
        return false;
    }

    @Override
    public List<TokenType> getMatchableTokens() {
        return null;
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        return null;
    }
}
