package fi.jgke.minpascal.astparser.nodes;

import fi.jgke.minpascal.data.Position;

import java.util.List;

public class ListAstNode extends AstNode {
    private final List<AstNode> content;

    public ListAstNode(String name, List<AstNode> content, Position position) {
        super(name, position);
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
        if (content.size() != 1) throw new AssertionError();
        if (!availableSet) throw new AssertionError();
        return new MappingAstNode<>(this, content.get(0));
    }
}
