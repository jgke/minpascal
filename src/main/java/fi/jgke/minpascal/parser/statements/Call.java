package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.blocks.Arguments;
import fi.jgke.minpascal.parser.nodes.ArgumentsNode;
import fi.jgke.minpascal.parser.nodes.CallNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.CLOSEPAREN;
import static fi.jgke.minpascal.data.TokenType.IDENTIFIER;
import static fi.jgke.minpascal.data.TokenType.OPENPAREN;

public class Call implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(IDENTIFIER);
    }

    @Override
    public boolean matches(ParseQueue queue) {
        return queue.isNext(OPENPAREN);
    }

    @Override
    public CallNode parse(ParseQueue queue) {
        throw new CompilerException("parseWithIdentifier() called on Call without identifier argument");
    }

    public CallNode parseWithIdentifier(Token<String> identifier, ParseQueue queue) {
        queue.getExpectedToken(OPENPAREN);
        ArgumentsNode arguments = new Arguments().parse(queue);
        queue.getExpectedToken(CLOSEPAREN);
        return new CallNode(identifier, arguments);
    }
}
