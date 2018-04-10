package fi.jgke.minpascal.compiler.nodes;

import lombok.Data;

import java.util.Optional;

@Data
public class Statement {
    private final Optional<CVariable> variableDefinition;
    private final Optional<Expression> expression;
    private final Optional<Conditional> conditional;

    public static <T> T parse(Object simple) {
        return null;
    }
}
