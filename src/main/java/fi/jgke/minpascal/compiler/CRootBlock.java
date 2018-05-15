package fi.jgke.minpascal.compiler;

import fi.jgke.minpascal.compiler.nodes.CFunction;
import lombok.Data;

import java.util.List;

@Data
public class CRootBlock {
    private final List<String> libraries;
    private final List<String> stdFunctions;
    private final List<CFunction> functions;
}
