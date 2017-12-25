package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.TypeNode;

import java.util.Arrays;
import java.util.List;

public class Type implements Parsable {
    private final Parsable[] children = new Parsable[]{new SimpleType(), new ArrayType()};

    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(children);
    }

    @Override
    public TypeNode parse(ParseQueue queue) {
        return new TypeNode(queue.any(children));
    }
}
