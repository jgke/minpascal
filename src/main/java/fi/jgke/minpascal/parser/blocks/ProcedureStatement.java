package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.FunctionNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class ProcedureStatement implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(PROCEDURE);
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        queue.getExpectedToken(PROCEDURE);
        Token identifier = queue.getExpectedToken(IDENTIFIER);
        queue.getExpectedToken(OPENPAREN);
        TreeNode parameters = new Parameters().parse(queue);
        queue.getExpectedTokens(CLOSEPAREN, SEMICOLON);
        TreeNode body = new Block().parse(queue);
        return new FunctionNode(identifier, parameters, body);
    }
}
