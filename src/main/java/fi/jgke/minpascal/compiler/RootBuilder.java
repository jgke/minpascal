package fi.jgke.minpascal.compiler;

import fi.jgke.minpascal.compiler.nodes.CFunction;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.parser.nodes.BlockNode;
import fi.jgke.minpascal.parser.nodes.FunctionNode;
import fi.jgke.minpascal.parser.nodes.ParametersNode;
import fi.jgke.minpascal.parser.nodes.StatementNode;

import java.util.Collections;
import java.util.List;

public class RootBuilder {
    BlockNode root;

    public RootBuilder(BlockNode root) {
        this.root = root;
    }

    private FunctionNode fromStatement(List<StatementNode> statementNode) {
        return new FunctionNode(
                Token.token(TokenType.IDENTIFIER, "main", new Position(0, 0)),
                new ParametersNode(Collections.emptyList()),
                root
        );
    }

    public void build(CBuilder output) {
        output.addFunction("main", fromStatement(root.getChildren()));
        output.append("\n");
    }

    public static CFunction parse(BlockNode root) {
        return new CFunction(
                "main",
                Collections.emptyList(),
                CType.CVOID,
               null // CBlock.parse(root)
        );
    }
}
