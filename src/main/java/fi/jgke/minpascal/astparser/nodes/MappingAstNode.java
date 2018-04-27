package fi.jgke.minpascal.astparser.nodes;

import fi.jgke.minpascal.exception.CompilerException;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
public class MappingAstNode<T> {
    @Delegate
    private final AstNode instance;
    private final Map<String, Function<AstNode, T>> mappers = new HashMap<>();

    public MappingAstNode<T> map(String name, Function<AstNode, T> handler) {
        if (!instance.getAvailableNames().contains(name)) {
            throw new CompilerException("Name " + name + " not found");
        }
        mappers.put(name, handler);
        return this;
    }

    public T unwrap() {
        if (instance.getAvailableNames().size() != mappers.size()) {
            throw new CompilerException("Unhandled node");
        }
        return mappers.get(instance.getName()).apply((AstNode) getContent());
    }
}
