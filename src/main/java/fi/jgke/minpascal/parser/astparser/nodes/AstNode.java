package fi.jgke.minpascal.parser.astparser.nodes;

import fi.jgke.minpascal.exception.CompilerException;

import java.util.List;
import java.util.function.Consumer;

public interface AstNode {

    default String getName() {
        return "";
    }

    default AstNode leaf(Consumer<String> fn) {
        return this;
    }

    default AstNode list(Consumer<List<AstNode>> handler) {
        return this;
    }

    default void error() {
        throw new CompilerException("Unhandled node");
    }
}
