package fi.jgke.minpascal.astparser.nodes;

import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class LeafNode implements AstNode {
    private final String name;
    private final String content;

    @Override
    public AstNode leaf(Consumer<String> fn) {
        fn.accept(content);
        return HandledAstNode.instance;
    }

    @Override
    public String toString() {
        return ":" + name + '~' + content;
    }
}
