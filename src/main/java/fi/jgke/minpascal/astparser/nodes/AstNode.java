package fi.jgke.minpascal.astparser.nodes;

import fi.jgke.minpascal.exception.CompilerException;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.jgke.minpascal.util.Formatter.formatTree;

public abstract class AstNode {
    @Getter
    @Setter
    private String name;
    @Getter
    private Set<String> availableNames = new HashSet<>();
    protected boolean availableSet = false;

    public AstNode(String name) {
        this.name = name;
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
        throw new UnsupportedOperationException();
    }

    public <T> MappingAstNode<T> toMap() {
        throw new UnsupportedOperationException();
    }

    public Optional<AstNode> toOptional() {
        return Optional.of(this);
    }

    public AstNode getFirstChild(String withName) {
        return getList().stream()
                .filter(o -> o.getName().equals(withName))
                .findFirst()
                .orElseThrow(() -> new CompilerException("Child " + withName + " not found, available: "
                        + getList().stream().map(AstNode::getName).collect(Collectors.joining(", ")) + "/" + availableNames.toString()
                ));
    }

    public void debug() {
        System.out.println(formatTree(this.toString()));
        System.out.flush();
    }
}
