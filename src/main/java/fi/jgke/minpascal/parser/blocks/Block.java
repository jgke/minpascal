package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.BlockNode;
import fi.jgke.minpascal.parser.nodes.StatementNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class Block implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(BEGIN);
    }

    @Override
    public BlockNode parse(ParseQueue queue) {
        Statement statement = new Statement();
        queue.getExpectedToken(BEGIN);
        List<StatementNode> children = queue.collectBy(statement::parse, true, SEMICOLON, statement::matches, true);
        queue.getExpectedToken(END);
        return new BlockNode(children);
    }
}
