package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.exception.ParseException;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.SimpleTypeNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.IDENTIFIER;

public class SimpleType implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(IDENTIFIER);
    }

    @Override
    public SimpleTypeNode parse(ParseQueue queue) {
        Token<String> identifier = queue.getIdentifier();
        switch (identifier.getValue().toLowerCase()) {
            case "integer":
                break;
            case "boolean":
                break;
            case "string":
                break;
            case "real":
                break;
            default:
                throw new ParseException(identifier.getPosition(), "Unknown type: " + identifier.getValue());
        }
        return new SimpleTypeNode(identifier);
    }
}
