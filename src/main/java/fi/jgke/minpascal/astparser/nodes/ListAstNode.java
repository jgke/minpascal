package fi.jgke.minpascal.astparser.nodes;

import fi.jgke.minpascal.util.OptionalList;

import java.util.List;

public class ListAstNode extends AstNode {
    private final List<AstNode> content;

    public ListAstNode(String name, List<AstNode> content) {
        super(name);
        this.content = content;//content.stream() .filter(p -> !((p instanceof EmptyNode) && p.getName().equals("_epsilon"))) .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String s = content.toString();
        return (availableSet ? "?:" : "L:") + getName() + " (" + s.substring(1, s.length() - 1) + ")";
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public OptionalList<AstNode> getList() {
        return new OptionalList<>(content);
    }
}
