package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.expressions.Expression;
import fi.jgke.minpascal.parser.nodes.ReturnNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.RETURN;

public class Return implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(RETURN);
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        queue.getExpectedToken(RETURN);
        return new ReturnNode(new Expression().parse(queue));
    }
}
