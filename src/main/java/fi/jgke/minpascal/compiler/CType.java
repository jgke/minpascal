package fi.jgke.minpascal.compiler;

import com.google.common.collect.Streams;
import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.nodes.CFunction;
import fi.jgke.minpascal.compiler.nodes.CVariable;
import fi.jgke.minpascal.exception.CompilerException;
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
    private final Optional<CType> returnType;
    private final List<CType> parameters;

    private static final Map<String, String> pascalToCMap;

    static {
        pascalToCMap = new HashMap<>();
        pascalToCMap.put("boolean", "bool");
        pascalToCMap.put("integer", "int");
        pascalToCMap.put("string", "char *");
    }

    public CType(CType returnType, List<CType> parameters) {
        this("void", Optional.of(returnType), parameters);
    }

    public CType(String me) {
        this(me, Optional.empty(), Collections.emptyList());
    }

    public static CType fromFunction(CFunction node) {
        List<CVariable> parameters = node.getParameters();
        CType returnType = node.getReturnType();
        return new CType(returnType,
                parameters.stream()
                        .map(CVariable::getType)
                        .collect(Collectors.toList()));
    }

    private static String getType(AstNode simpleTypeNode) {
        return simpleTypeNode.<String>toMap()
                .map("int", AstNode::getContentString)
                .map("str", AstNode::getContentString)
                .unwrap();
    }

    public static CType fromTypeNode(AstNode typeNode) {
        String type = typeNode.<String>toMap()
                .map("SimpleType", CType::getType)
                .map("ArrayType", atype -> getType(atype.getFirstChild("SimpleType")))
                .unwrap();
        type = pascalToCMap.getOrDefault(type, null);
        if (type == null) {
            throw new CompilerException("Invalid type " + typeNode);
        }
        type += typeNode.getOptionalChild("ArrayType")
                .map(arrayTypeNode -> " *").orElse("");
        return new CType(type);
    }

    @Override
    public String toString() {
        return returnType
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
        if (!returnType.isPresent()) throw new AssertionError();
        return returnType.get().toString() + " (*" + identifier + ")" + formatCall(parameters);
    }

    // returnType (*identifier)(args)
    // type identifier
    public String toDeclaration(String identifier) {
        return this.returnType
                .map($ -> formatFunctionPointer(identifier))
                .orElseGet(() -> this.toString() + " " + identifier);
    }

    public String toFunctionDeclaration(List<String> argumentIdentifiers, String name) {
        if (!returnType.isPresent()) throw new AssertionError();
        if (argumentIdentifiers.size() != parameters.size()) throw new AssertionError();
        return
                returnType.get().toString() + " " + name + "(" +
                        Streams.zip(parameters.stream(), argumentIdentifiers.stream(),
                                (type, identifier) -> type.toString() + " " + identifier
                        ).collect(Collectors.joining(", ")) + ")";
    }

    public String toFormat() {
        if (returnType.isPresent() || parameters.size() > 0)
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
