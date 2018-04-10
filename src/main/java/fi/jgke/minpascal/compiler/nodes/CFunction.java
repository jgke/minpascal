package fi.jgke.minpascal.compiler.nodes;

import fi.jgke.minpascal.compiler.CType;
import lombok.Data;

import java.util.List;

@Data
public class CFunction {
    private final String identifier;
    private final List<CVariable> parameters;
    private final CType returnType;
    private final List<Statement> statements;
}
