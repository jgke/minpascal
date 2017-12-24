package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.expressions.Variable;
import fi.jgke.minpascal.parser.nodes.ReadNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.COMMA;
import static fi.jgke.minpascal.data.TokenType.OPENPAREN;
import static fi.jgke.minpascal.data.TokenType.READ;

public class Read implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(READ);
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        queue.getExpectedTokens(READ, OPENPAREN);
        ArrayList<TreeNode> variables = new ArrayList<>();
        variables.add(new Variable().parse(queue));
        while(queue.isNext(COMMA)) {
            queue.getExpectedToken(COMMA);
            variables.add(new Variable().parse(queue));
        }
        return new ReadNode(variables);
    }
}
