package fi.jgke.minpascal.compiler;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.nodes.CFunction;

public class RootBuilder {
    AstNode root;

    public RootBuilder(AstNode root) {
        this.root = root;
    }

    public void build(CBuilder output) {
        output.addFunction("main", CFunction.fromBlock(root.getFirstChild("Block")));
        output.append("\n");
    }
}
