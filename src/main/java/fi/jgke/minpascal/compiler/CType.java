package fi.jgke.minpascal.compiler;

import com.google.common.collect.Streams;
import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.nodes.CFunction;
import fi.jgke.minpascal.compiler.nodes.CVariable;
import fi.jgke.minpascal.exception.CompilerException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * C types handled as a linked list
 */
@EqualsAndHashCode(exclude = "ptrTo")
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
    private final Optional<CType> ptrTo;

    private static final Map<String, String> pascalToCMap;

    static {
        pascalToCMap = new HashMap<>();
        pascalToCMap.put("boolean", "bool");
        pascalToCMap.put("integer", "int");
        pascalToCMap.put("string", "char *");
    }

    public CType(CType returnType, List<CType> parameters) {
        this("void", Optional.of(returnType), parameters, Optional.empty());
    }

    public CType(String me) {
        this(me, Optional.empty(), Collections.emptyList(), Optional.empty());
    }

    public CType(String me, Optional<CType> returnType, List<CType> parameters) {
        this(me, returnType, parameters, Optional.empty());
    }

    public static CType ptrTo(CType cType) {
        return new CType(null, Optional.empty(), Collections.emptyList(), Optional.of(cType));
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
                .map("real", AstNode::getContentString)
                .unwrap();
    }

    public static CType fromTypeNode(AstNode typeNode, boolean ptr) {
        String type = typeNode.<String>toMap()
                .map("SimpleType", CType::getType)
                .map("ArrayType", atype -> getType(atype
                        .getFirstChild("Expression")
                        .getFirstChild("cb")
                        .getFirstChild("SimpleType")))
                .unwrap();
        type = pascalToCMap.getOrDefault(type, null);
        if (type == null) {
            throw new CompilerException("Invalid type " + typeNode);
        }
        //type += typeNode.getOptionalChild("ArrayType")
        //        .map(arrayTypeNode -> " *").orElse("");
        CType cType = new CType(type);
        if (ptr || typeNode.getOptionalChild("ArrayType").isPresent()) {
            cType = CType.ptrTo(cType);
        }
        return cType;
    }

    @Override
    public String toString() {
        return returnType
                .map($ -> formatFunctionPointer(""))
                .orElse(getMe());
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
    public String toDeclaration(String identifier, Optional<String> initializer) {
        String s = this.returnType
                .map($ -> formatFunctionPointer(identifier))
                .orElseGet(() ->
                        this.ptrTo.map(to -> to.toString() + " *" + identifier)
                                .orElseGet(() -> this.toString() + " " + identifier));
        return initializer.map(id ->
                s + " = malloc(sizeof(int) * (" + id + " + 1));\n" +
                        identifier + "[0] = " + id + ";\n" +
                        identifier + "++")
                .orElse(s);
    }

    public String toFunctionDeclaration(List<String> argumentIdentifiers, String name) {
        if (!returnType.isPresent()) throw new AssertionError();
        if (argumentIdentifiers.size() != parameters.size()) throw new AssertionError();
        return
                returnType.get().toString() + " " + name + "(" +
                        Streams.zip(parameters.stream(), argumentIdentifiers.stream(),
                                (type, identifier) ->
                                        type.getPtrTo().map(to -> to.toString() + " *" + identifier
                                        ).orElseGet(() -> type.toString() + " " + identifier)
                        ).collect(Collectors.joining(", ")) + ")";
    }

    public String toFormat() {
        CType me = this.dereferenceMaybe();
        if (me.returnType.isPresent() || me.parameters.size() > 0)
            return "%p";
        if (me.equals(CINTEGER)) return "%d";
        if (me.equals(CDOUBLE)) return "%f";
        if (me.equals(CBOOLEAN)) return "%d";
        if (me.equals(CSTRING)) return "%s";
        return "%p";
    }

    public String defaultValue() {
        if (this.equals(CINTEGER) || this.equals(CBOOLEAN)) return "0";
        if (this.equals(CDOUBLE)) return "0.0d";
        return "NULL";
    }

    public CType dereferenceMaybe() {
        return this.ptrTo.orElse(this);
    }

    private String getMe() {
        return this.dereferenceMaybe().me;
    }

    public Optional<CType> getReturnType() {
        return this.dereferenceMaybe().returnType;
    }

    public List<CType> getParameters() {
        return this.dereferenceMaybe().parameters;
    }

    public Optional<CType> getPtrTo() {
        return ptrTo;
    }
}
