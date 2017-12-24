package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.parser.base.Parsable;

import java.util.Arrays;
import java.util.List;

public class SimpleStatement implements Parsable {
    private static final Parsable[] children = new Parsable[]{
            new AssignmentStatement(),
            new Call(),
            new Return(),
            new Read(),
            new Write(),
            new Assert()
    };

    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(children);
    }
}
