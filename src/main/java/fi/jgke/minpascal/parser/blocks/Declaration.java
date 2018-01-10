package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.DeclarationNode;
import fi.jgke.minpascal.parser.nodes.FunctionNode;
import fi.jgke.minpascal.parser.nodes.VarDeclarationNode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Declaration implements Parsable {
    private final Parsable[] children = new Parsable[]{
            new VarStatement(), new ProcedureStatement(), new FunctionStatement()
    };

    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(children);
    }

    @Override
    public DeclarationNode parse(ParseQueue queue) {
        TreeNode content = queue.any(children);

        if (content instanceof VarDeclarationNode) {
            return new DeclarationNode(Optional.of((VarDeclarationNode) content), Optional.empty());
        }

        return new DeclarationNode(Optional.empty(), Optional.of((FunctionNode) content));
    }
}
