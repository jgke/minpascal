package fi.jgke.minpascal.astparser.nodes;

import java.util.List;

public class ListAstNode extends AstNode {
    private final List<AstNode> content;

    public ListAstNode(String name, List<AstNode> content) {
        super(name);
        this.content = content;
    }

    @Override
    public String toString() {
        String s = content.toString();
        return (availableSet ? "?:" : "L:") + getName() + " (" + s.substring(1, s.length() - 1) + ")";
    }

    @Override
    public Object getContent() {
        return this;
    }

    @Override
    public List<AstNode> getList() {
        return content;
    }

    public <T> MappingAstNode<T> toMap() {
        assert content.size() == 1;
        assert availableSet;
        return new MappingAstNode<>(this, content.get(0));
    }
}
