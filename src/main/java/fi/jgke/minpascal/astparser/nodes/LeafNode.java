package fi.jgke.minpascal.astparser.nodes;

import java.util.Collections;

public class LeafNode extends AstNode {
    private final Object content;

    public LeafNode(String name, Object content) {
        super(name);
        this.content = content;
        this.setAvailableNames(Collections.singleton(name));
    }

    @Override
    public String toString() {
        return ".:" + getName() + '~' + content;
    }

    @Override
    public Object getContent() {
        return content;
    }
}
