package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.BlockNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.BEGIN;
import static fi.jgke.minpascal.data.TokenType.END;
import static fi.jgke.minpascal.data.TokenType.SEMICOLON;

public class Block implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(BEGIN);
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        Statement statement = new Statement();
        queue.getExpectedToken(BEGIN);
        List<TreeNode> children = queue.collectBy(statement::parse, true, SEMICOLON, statement::matches, true);
        queue.getExpectedToken(END);
        return new BlockNode(children);
    }
}
