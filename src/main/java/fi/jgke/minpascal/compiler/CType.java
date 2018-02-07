package fi.jgke.minpascal.compiler;

import com.google.common.collect.Streams;
import fi.jgke.minpascal.parser.nodes.ArrayTypeNode;
import fi.jgke.minpascal.parser.nodes.FunctionNode;
import fi.jgke.minpascal.parser.nodes.TypeNode;
import fi.jgke.minpascal.parser.nodes.VarDeclarationNode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * C types handled as a linked list
 */
@Data
@AllArgsConstructor
public class CType {

    public static final CType BOOLEAN = new CType("boolean");
    public static final CType INTEGER = new CType("int");
    public static final CType DOUBLE = new CType("double");
    public static final CType STRING  = new CType("char *");

    private final String me;
    private final Optional<CType> call;
    private final List<CType> sibling;

    private static final Map<String, String> pascalToCMap;

    static {
        pascalToCMap = new HashMap<>();
        pascalToCMap.put("boolean", "bool");
        pascalToCMap.put("integer", "int");
        pascalToCMap.put("string", "char *");
    }

    public CType(String me) {
        this(me, Optional.empty(), Collections.emptyList());
    }

    public CType(Optional<TypeNode> type, Optional<CType> call, List<CType> siblings) {
        this(type.map(CType::fromPascal).orElse("void"), call, siblings);
    }

    public CType(Optional<TypeNode> type) {
        this(type.map(CType::fromPascal).orElse("void"));
    }

    public CType(TypeNode typeNode) {
        this(Optional.of(typeNode));
    }

    public static CType fromFunction(FunctionNode node) {
        CType returnValue = new CType(node.getReturnType());
        List<VarDeclarationNode> declarations = node.getParams().getDeclarations();
        List<CType> params = declarations.stream()
                .flatMap(subNode -> subNode.getIdentifiers().stream()
                        .map($ -> subNode.getType()))
                .map(CType::new)
                .collect(Collectors.toList());
        return new CType(
                declarations.size() == 0
                        ? Optional.empty()
                        : Optional.ofNullable(declarations.get(0))
                        .map(VarDeclarationNode::getType),
                Optional.of(returnValue),
                params
        );
    }

    private static String fromPascal(TypeNode typeNode) {
        String type = pascalToCMap.get(
                typeNode.getSimpleType()
                        .orElseGet(() -> typeNode.getArrayType().map(ArrayTypeNode::getType).get())
                        .getType().getType().toString());
        type += typeNode.getArrayType().map(arrayTypeNode -> "*").orElse("");
        return type;
    }

    @Override
    public String toString() {
        return call
                .map($ -> formatFunctionPointer(""))
                .orElse(me);
    }

    private static String formatCall(List<CType> types) {
        return "(" +
                types.stream().map(CType::toString).collect(Collectors.joining(", ")) +
                ")";
    }

    // returnType (*identifier)(args)
    private String formatFunctionPointer(String identifier) {
        assert call.isPresent();
        return call.get().toString() + " (*" + identifier + ")" + formatCall(sibling);
    }

    // returnType (*identifier)(args)
    // type identifier
    public String toDeclaration(String identifier) {
        return this.call
                .map($ -> formatFunctionPointer(identifier))
                .orElseGet(() -> this.toString() + " " + identifier);
    }

    public String toFunctionDeclaration(List<String> argumentIdentifiers, String name) {
        assert call.isPresent();
        assert argumentIdentifiers.size() == sibling.size();
        return
                call.get().toString() + " " + name + "(" +
                        Streams.zip(sibling.stream(), argumentIdentifiers.stream(),
                                (type, identifier) -> type.toString() + " " + identifier
                        ).collect(Collectors.joining(", ")) + ")";
    }
}
