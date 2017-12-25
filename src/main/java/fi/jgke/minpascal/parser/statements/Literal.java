package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.LiteralNode;

import java.util.Arrays;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class Literal implements Parsable {
    private static final Parsable[] children = new Parsable[]{
            INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL
    };

    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(children);
    }

    @Override
    public LiteralNode parse(ParseQueue queue) {
        return new LiteralNode(queue.any(children));
    }
}
