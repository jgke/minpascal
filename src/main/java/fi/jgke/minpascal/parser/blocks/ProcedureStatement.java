package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.BlockNode;
import fi.jgke.minpascal.parser.nodes.FunctionNode;
import fi.jgke.minpascal.parser.nodes.ParametersNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class ProcedureStatement implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(PROCEDURE);
    }

    @Override
    public FunctionNode parse(ParseQueue queue) {
        queue.getExpectedToken(PROCEDURE);
       Token<String> identifier = queue.getIdentifier();
        queue.getExpectedToken(OPENPAREN);
        ParametersNode parameters = new Parameters().parse(queue);
        queue.getExpectedTokens(CLOSEPAREN, SEMICOLON);
        BlockNode body = new Block().parse(queue);
        return new FunctionNode(identifier, parameters, body);
    }
}
