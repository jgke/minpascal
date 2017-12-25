package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.blocks.Arguments;
import fi.jgke.minpascal.parser.nodes.ArgumentsNode;
import fi.jgke.minpascal.parser.nodes.CallNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.IDENTIFIER;
import static fi.jgke.minpascal.data.TokenType.OPENPAREN;

public class Call implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(IDENTIFIER);
    }

    @Override
    public CallNode parse(ParseQueue queue) {
        Token identifier = queue.getExpectedToken(IDENTIFIER);
        queue.getExpectedToken(OPENPAREN);
        ArgumentsNode arguments = new Arguments().parse(queue);
        return new CallNode(identifier, arguments);
    }
}
