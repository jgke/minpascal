package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.blocks.Block;
import fi.jgke.minpascal.parser.nodes.BlockNode;
import fi.jgke.minpascal.parser.nodes.IfThenNode;
import fi.jgke.minpascal.parser.nodes.StructuredStatementNode;
import fi.jgke.minpascal.parser.nodes.WhileNode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StructuredStatement implements Parsable {
    private static final Parsable[] children = new Parsable[]{
            new Block(),
            new IfStatement(),
            new WhileStatement()
    };

    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(children);
    }

    @Override
    public StructuredStatementNode parse(ParseQueue queue) {
        TreeNode node = queue.any(children);

        BlockNode blockNode = null;
        IfThenNode ifNode = null;
        WhileNode whileNode = null;

        if(node instanceof BlockNode) blockNode = (BlockNode) node;
        if(node instanceof IfThenNode) ifNode = (IfThenNode) node;
        if(node instanceof WhileNode) whileNode = (WhileNode) node;

        assert blockNode != null || ifNode != null || whileNode != null;

        return new StructuredStatementNode(
                Optional.ofNullable(blockNode),
                Optional.ofNullable(ifNode),
                Optional.ofNullable(whileNode)
        );
    }
}
