package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.blocks.Arguments;
import fi.jgke.minpascal.parser.nodes.WriteNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class Write implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(WRITELN);
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        queue.getExpectedTokens(WRITELN, OPENPAREN);
        TreeNode arguments = new Arguments().parse(queue);
        queue.getExpectedTokens(CLOSEPAREN);
        return new WriteNode(arguments);
    }
}
