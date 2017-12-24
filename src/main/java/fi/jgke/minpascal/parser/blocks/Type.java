package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.parser.base.Parsable;

import java.util.Arrays;
import java.util.List;

public class Type implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(new SimpleType(), new ArrayType());
    }
}
