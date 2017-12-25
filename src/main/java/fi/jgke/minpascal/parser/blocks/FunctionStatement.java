package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.FunctionNode;
import fi.jgke.minpascal.parser.nodes.ParametersNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class FunctionStatement implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(FUNCTION);
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        // "function" <id> "(" parameters ")" ":" <type> ";" <block>
        queue.getExpectedToken(FUNCTION);
        Token identifier = queue.getExpectedToken(IDENTIFIER);
        queue.getExpectedToken(OPENPAREN);
        ParametersNode params = new Parameters().parse(queue);
        queue.getExpectedTokens(CLOSEPAREN, COLON);
        TreeNode returnType = new Type().parse(queue);
        queue.getExpectedToken(SEMICOLON);
        TreeNode body = new Block().parse(queue);
        return new FunctionNode(identifier, params, body, returnType);
    }
}
