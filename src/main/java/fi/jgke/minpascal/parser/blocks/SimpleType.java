package fi.jgke.minpascal.parser.blocks;

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
        return new SimpleTypeNode(queue.getExpectedToken(IDENTIFIER));
    }
}
