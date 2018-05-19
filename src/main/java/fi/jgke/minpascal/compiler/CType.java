package fi.jgke.minpascal.compiler;

import com.google.common.collect.Streams;
import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.exception.TypeError;
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
        pascalToCMap.put("real", "double");
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

    private static String getType(AstNode simpleTypeNode) {
        return simpleTypeNode.<String>toMap()
                .map("int", $ -> "integer")
                .map("str", $ -> "string")
                .map("real", $ -> "real")
                .map("boolean", $ -> "boolean")
                .unwrap();
    }

    public static CType fromTypeNode(AstNode typeNode, boolean ptr) {
        String parsedType = typeNode.<String>toMap()
                .map("SimpleType", CType::getType)
                .map("ArrayType", atype -> getType(atype
                        .getFirstChild("Expression")
                        .getFirstChild("cb")
                        .getFirstChild("SimpleType")))
                .unwrap();
        String type = pascalToCMap.getOrDefault(parsedType, null);
        if (type == null) {
            throw new CompilerException("Invalid type " + parsedType);
        }
        //type += typeNode.getOptionalChild("ArrayType")
        //        .map(arrayTypeNode -> " *").orElse("");
        CType cType = new CType(type);
        if (ptr || typeNode.getOptionalChild("ArrayType").isPresent()) {
            cType = CType.ptrTo(cType);
        }
        return cType;
    }

    //Just the type (for eg. casts)
    public String formatType() {
        return returnType
                .map($ -> formatFunctionPointer(""))
                .orElse(getMe());
    }

    private static String formatCall(List<CType> types) {
        return "(" +
                types.stream().map(CType::formatType).collect(Collectors.joining(", ")) +
                ")";
    }

    // returnType (*identifier)(args)
    private String formatFunctionPointer(String identifier) {
        if (!returnType.isPresent()) throw new AssertionError();
        return returnType.get().formatType() + " (*" + identifier + ")" + formatCall(parameters);
    }

    // returnType (*identifier)(args)
    // type identifier
    public String toDeclaration(String identifier, Optional<String> initializer) {
        String s = this.returnType
                .map($ -> formatFunctionPointer(identifier))
                .orElseGet(() ->
                        this.ptrTo.map(to -> to.formatType() + " *" + identifier)
                                .orElseGet(() -> this.formatType() + " " + identifier));
        return initializer.map(id ->
                s + " = malloc(sizeof(int) * (" + id + " + 1));\n" +
                        identifier + "[0] = " + id + ";\n" +
                        identifier + "++")
                .orElse(s);
    }

    public String toFunctionDeclaration(List<String> argumentIdentifiers, String name) {
        if (!returnType.isPresent()) throw new AssertionError();
        if (argumentIdentifiers.size() != parameters.size()) throw new AssertionError();
        return returnType.get().formatType() + " " + name + "(" +
                Streams.zip(parameters.stream(), argumentIdentifiers.stream(),
                        (type, identifier) ->
                                type.getPtrTo().map(to -> to.formatType() + " *" + identifier
                                ).orElseGet(() -> type.formatType() + " " + identifier)
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

    public boolean isAssignable(CType to) {
        to = to.dereferenceMaybe();
        CType me = this.dereferenceMaybe();
        if (to.equals(me))
            return true;
        if (me.equals(CINTEGER)) {
            return to.equals(CINTEGER) || to.equals(CDOUBLE);
        }
        return false;
    }

    public String assignTo(CType to, String identifier, Position position) {
        if (!isAssignable(to)) {
            throw new TypeError("Cannot assign " + this.formatType() + " to " + to.formatType(), position);
        }
        return this.getPtrTo().map($ ->
                to.getPtrTo()
                        .map($$ -> identifier)
                        .orElse("*" + identifier))
                .orElse(to.getPtrTo()
                        .map($ -> "&" + identifier)
                        .orElse(identifier));
    }
}
