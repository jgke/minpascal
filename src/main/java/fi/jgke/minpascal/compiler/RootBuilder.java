package fi.jgke.minpascal.compiler;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.nodes.CBlock;

import java.util.stream.Collectors;

public class RootBuilder {
    AstNode root;

    public RootBuilder(AstNode root) {
        this.root = root;
    }

    public void build(CBuilder output) {
        root.debug(3);
        root.getFirstChild("more").getList().stream()
                .flatMap(CBlock::fromDeclaration)
                .forEach(c -> output.append(c.getData()));
        output.append("\nint main() {");
        output.append(CBlock.parse(root.getFirstChild("Block")).getContents()
                .stream().map(CBlock.Content::getData).collect(Collectors.joining("")));
        output.append("\n}");
    }
}
