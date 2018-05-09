package fi.jgke.minpascal.compiler;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.nodes.CBlock;
import fi.jgke.minpascal.compiler.nodes.CFunction;

public class RootBuilder {
    AstNode root;

    public RootBuilder(AstNode root) {
        this.root = root;
    }

    public void build(CBuilder output) {
        root.debug(3);
        System.out.println("more");
        root.getFirstChild("more").getList() .stream()
                .flatMap(CBlock::fromDeclaration)
                .forEach(c -> output.append(c.getData()));
        System.out.println("amore, main");
        output.addFunction("main", CFunction.fromBlock(root.getFirstChild("Block")));
        System.out.println("amain");
        output.append("\n");
    }
}
