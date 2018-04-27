package fi.jgke.minpascal.astparser.nodes;

import fi.jgke.minpascal.exception.CompilerException;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import java.util.*;
import java.util.function.Function;

@AllArgsConstructor
public class MappingAstNode<T> {
    @Delegate
    private final AstNode parent;
    private final AstNode child;

    private final Map<String, Function<AstNode, T>> mappers = new HashMap<>();

    public MappingAstNode<T> map(String name, Function<AstNode, T> handler) {
        if (!parent.getAvailableNames().contains(name)) {
            throw new CompilerException("Name " + name + " not found (supported: " + parent.getAvailableNames() + ")");
        }
        mappers.put(name, handler);
        return this;
    }

    public T unwrap() {
        Set<String> names = new HashSet<>(parent.getAvailableNames());
        names.addAll(mappers.keySet());
        names.removeAll(mappers.keySet());
        if (!names.isEmpty()) {
            throw new CompilerException("Unhandled node(s): " + names);
        }
        return mappers.get(child.getName()).apply(child);
    }
}
