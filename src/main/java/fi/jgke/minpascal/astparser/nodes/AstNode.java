package fi.jgke.minpascal.astparser.nodes;

import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.util.OptionalList;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Consumer;

import static fi.jgke.minpascal.util.Formatter.formatTree;

public abstract class AstNode {
    @Getter
    @Setter
    private String name;
    @Getter
    private Set<String> availableNames = new HashSet<>();
    private final Map<String, Consumer<AstNode>> visitors = new HashMap<>();
    protected boolean availableSet = false;

    public AstNode(String name) {
        this.name = name;
    }

    public AstNode setAvailableNames(Set<String> availableNames) {
        this.availableNames = availableNames;
        this.availableSet = true;
        return this;
    }

    public AstNode chain(String name, Consumer<AstNode> handler) {
        if (!availableNames.contains(name)) {
            throw new CompilerException("Name " + name + " not found");
        }
        visitors.put(name, handler);
        return this;
    }

    public abstract Object getContent();

    public String getContentString() {
        return (String) getContent();
    }

    public OptionalList<AstNode> getList() {
        return new OptionalList<>(Collections.singletonList((AstNode) getContent()));
    }

    public void visit() {
        if (availableNames.size() != visitors.size()) {
            throw new CompilerException("Unhandled node");
        }

        visitors.get(name).accept((AstNode) getContent());
    }

    public <T> MappingAstNode<T> toMap() {
        return new MappingAstNode<>(this);
    }

    public Optional<AstNode> toOptional() {
        return Optional.of((AstNode) getContent());
    }

    public AstNode getFirstChild(String withName) {
        return getList() .stream()
                .filter(o -> o.isPresent() && o.get().getName().equals(withName))
                .findFirst()
                .get().get();
    }

    public void debug() {
        System.out.println(formatTree(this.toString()));
    }
}
