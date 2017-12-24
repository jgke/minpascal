package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.blocks.Block;

import java.util.Arrays;
import java.util.List;

public class StructuredStatement implements Parsable {
    private static final Parsable[] children = new Parsable[]{
            new Block(),
            new IfStatement(),
            new WhileStatement()
    };

    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(children);
    }
}
