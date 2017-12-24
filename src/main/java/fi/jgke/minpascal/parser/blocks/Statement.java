package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.statements.SimpleStatement;
import fi.jgke.minpascal.parser.statements.StructuredStatement;

import java.util.Arrays;
import java.util.List;

public class Statement implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(new SimpleStatement(), new StructuredStatement(), new Declaration());
    }
}
