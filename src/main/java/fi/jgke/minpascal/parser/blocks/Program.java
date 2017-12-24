package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class Program implements Parsable {
    @Override
    public List<Parsable> getParsables() {
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
