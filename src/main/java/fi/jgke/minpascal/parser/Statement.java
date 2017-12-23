package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.data.TreeNode;

import java.util.List;

import static fi.jgke.minpascal.parser.ParseUtils.collectTokenTypes;

public class Statement implements Parsable {
    private static final Parsable[] children = new Parsable[]{
            new SimpleStatement(),
            new StructuredStatement(),
            new Declaration()
    };

    @Override
    public boolean matches(ParseQueue queue) {
        return queue.anyMatches(children);
    }

    @Override
    public List<TokenType> getMatchableTokens() {
        return collectTokenTypes(children);
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        return queue.any(children);
    }
}
