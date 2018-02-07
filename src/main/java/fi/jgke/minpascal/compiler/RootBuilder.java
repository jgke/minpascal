package fi.jgke.minpascal.compiler;

import fi.jgke.minpascal.CBuilder;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.parser.nodes.BlockNode;
import fi.jgke.minpascal.parser.nodes.FunctionNode;
import fi.jgke.minpascal.parser.nodes.ParametersNode;
import fi.jgke.minpascal.parser.nodes.StatementNode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RootBuilder {
    BlockNode root;

    public RootBuilder(BlockNode root) {
        this.root = root;
    }

    private FunctionNode fromStatement(List<StatementNode> statementNode) {
        return new FunctionNode(
                new Token(TokenType.IDENTIFIER, Optional.of("main"),
                        new Position(0, 0)),
                new ParametersNode(Collections.emptyList()),
                root
        );
    }

    public void build(CBuilder output) {
        output.addFunction("main", fromStatement(root.getChildren()));
    }
}
