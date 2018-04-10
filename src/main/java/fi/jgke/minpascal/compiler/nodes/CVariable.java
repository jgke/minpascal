package fi.jgke.minpascal.compiler.nodes;

import fi.jgke.minpascal.compiler.CType;
import lombok.Data;

@Data
public class CVariable implements Node {
    private final String identifier;
    private final CType type;
}
