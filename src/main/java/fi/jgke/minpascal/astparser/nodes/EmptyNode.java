package fi.jgke.minpascal.astparser.nodes;

import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.exception.CompilerException;

import java.util.Optional;

public class EmptyNode extends AstNode {
    public EmptyNode(String name, Position position) {
        super(name, position);
    }

    @Override
    public String toString() {
        return "EmptyNode:" + getName();
    }

    @Override
    public Object getContent() {
        throw new CompilerException("No content in an empty node");
    }

    @Override
    public Optional<AstNode> toOptional() {
        return Optional.empty();
    }
}
