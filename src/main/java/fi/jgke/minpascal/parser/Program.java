package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.data.TreeNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class Program implements Parsable {
    @Override
    public boolean matches(ParseQueue queue) {
        return queue.isNext(PROGRAM);
    }

    @Override
    public List<TokenType> getMatchableTokens() {
        return Collections.singletonList(PROGRAM);
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        queue.getExpectedTokens(PROGRAM);
        queue.getExpectedToken(IDENTIFIER);
        queue.getExpectedTokens(SEMICOLON);
        TreeNode block = new Block().parse(queue);
        queue.getExpectedToken(DOT);

        return block;
    }
}
