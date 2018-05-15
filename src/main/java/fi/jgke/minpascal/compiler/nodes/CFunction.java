package fi.jgke.minpascal.compiler.nodes;

import fi.jgke.minpascal.compiler.CType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CFunction {
    private final String identifier;
    private final CType returnType;
    private final CBlock statements;
}

