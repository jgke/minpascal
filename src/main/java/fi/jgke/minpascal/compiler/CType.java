package fi.jgke.minpascal.compiler;

import com.google.common.collect.Streams;
import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.nodes.CFunction;
import fi.jgke.minpascal.compiler.nodes.CVariable;
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

    public static final CType CBOOLEAN = new CType("bool");
    public static final CType CINTEGER = new CType("int");
    public static final CType CDOUBLE = new CType("double");
    public static final CType CSTRING = new CType("char *");
    public static final CType CVOID = new CType("void");

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

    public CType(Optional<AstNode> type, Optional<CType> call, List<CType> siblings) {
        this(type.map(CType::fromPascal).orElse("int"), call, siblings); // default to int so we can always store the result
    }

    public CType(Optional<AstNode> type) {
        this(type.map(CType::fromPascal).orElse("int"));
    }

    public CType(AstNode typeNode) {
        this(Optional.of(typeNode));
    }

    public static CType fromFunction(CFunction node) {
        List<CVariable> parameters = node.getParameters();
        CType returnType = node.getReturnType();
        return new CType(Optional.empty(),
                Optional.of(returnType),
                parameters.stream()
                        .map(CVariable::getType)
                        .collect(Collectors.toList()));
    }

    private static String fromPascal(AstNode typeNode) {
        throw new RuntimeException("Not impl");
        /*
        String type = pascalToCMap.get(
                typeNode.getSimpleType()
                        .orElseGet(typeNode.getArrayType().map(ArrayTypeNode::getType)::get)
                        .getType().getValue().toLowerCase());
        if (type == null) {
            throw new CompilerException("Invalid type " + typeNode);
        }
        type += typeNode.getArrayType().map(arrayTypeNode -> " *").orElse("");
        return type;
        */
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

    public String toFormat() {
        if (call.isPresent() || sibling.size() > 0)
            return "%p";
        if (this.equals(CINTEGER)) return "%d";
        if (this.equals(CDOUBLE)) return "%f";
        if (this.equals(CBOOLEAN)) return "%d";
        if (this.equals(CSTRING)) return "%s";
        return "%p";
    }

    public String defaultValue() {
        if (this.equals(CINTEGER) || this.equals(CBOOLEAN)) return "0";
        if (this.equals(CDOUBLE)) return "0.0d";
        return "NULL";
    }
}
