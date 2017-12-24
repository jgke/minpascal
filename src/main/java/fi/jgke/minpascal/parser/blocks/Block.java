package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.BEGIN;

public class Block implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(BEGIN);
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        queue.getExpectedToken(BEGIN);
        return new Statement().parse(queue);
    }
}
