package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.data.TreeNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class Block implements Parsable {
    @Override
    public boolean matches(ParseQueue queue) {
        return queue.isNext(BEGIN);
    }

    @Override
    public List<TokenType> getMatchableTokens() {
        return Collections.singletonList(BEGIN);
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        queue.getExpectedToken(BEGIN);
        return new Statement().parse(queue);
    }
}
