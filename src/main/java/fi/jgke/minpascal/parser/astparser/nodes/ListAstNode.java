package fi.jgke.minpascal.parser.astparser.nodes;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ListAstNode implements AstNode {
    private final String name;
    private final List<AstNode> content;

    public ListAstNode(String name, List<AstNode> content) {
        this.name = name;
        this.content = content.stream()
                .filter(p -> !((p instanceof EmptyNode) && p.getName().equals("_epsilon")))
                .collect(Collectors.toList());
    }

    @Override
    public AstNode list(Consumer<List<AstNode>> handler) {
        handler.accept(content);
        return HandledAstNode.instance;
    }

    @Override
    public String toString() {
        return "ListAstNode:" + name + ' ' + content;
    }
}
