package fi.jgke.minpascal.compiler.nodes;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.CType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CFunction {
    private final String identifier;
    private final List<CVariable> parameters;
    private final CType returnType;
    private final CBlock statements;

    public static CFunction fromDeclaration(AstNode declaration) {
        throw new RuntimeException("Not impl");
    }

    public static CFunction fromBlock(AstNode block) {
        return new CFunction("main",
                Collections.emptyList(),
                CType.CVOID,
                CBlock.parse(block));
    }
}

