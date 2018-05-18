package fi.jgke.minpascal.astparser.nodes;

import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.exception.CompilerException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

import static fi.jgke.minpascal.util.Formatter.formatTree;

public abstract class AstNode {
    @Getter
    @Setter
    private String name;
    @Getter
    private Set<String> availableNames = new HashSet<>();
    protected boolean availableSet = false;
    @Getter
    private final Position position;

    public AstNode(String name, Position position) {
        this.name = name;
        this.position = position;
    }

    public AstNode setAvailableNames(Set<String> availableNames) {
        this.availableNames = availableNames;
        this.availableSet = true;
        return this;
    }

    public abstract Object getContent();

    public String getContentString() {
        return (String) getContent();
    }

    public List<AstNode> getList() {
        return Collections.singletonList((AstNode)getContent());
    }

    public <T> MappingAstNode<T> toMap() {
        throw new UnsupportedOperationException();
    }

    public Optional<AstNode> toOptional() {
        return Optional.of(this);
    }

    public Optional<AstNode> getOptionalChild(String withName) {
        return getList().stream()
                .filter(o -> o.getName().equals(withName))
                .findFirst();
    }

    public AstNode getFirstChild(String withName) {
        return getOptionalChild(withName)
                .orElseThrow(() -> new CompilerException("Child " + withName + " not found, available: "
                        + getList().stream().map(AstNode::getName).collect(Collectors.joining(", ")) + "/" + availableNames.toString()
                ));
    }

    @SuppressWarnings("unused")
    public AstNode debug() {
        return debug(-1);
    }

    public AstNode debug(int limit) {
        System.out.println(formatTree(this.toString(), limit));
        System.out.flush();
        return this;
    }
}
