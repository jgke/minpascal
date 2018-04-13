package fi.jgke.minpascal.astparser.nodes;

import lombok.Data;

@Data
public class EmptyNode implements AstNode {
    private final String name;

    @Override
    public String toString() {
        return "EmptyNode:" + name;
    }
}
