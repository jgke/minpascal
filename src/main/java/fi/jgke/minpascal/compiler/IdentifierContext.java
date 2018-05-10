package fi.jgke.minpascal.compiler;

import fi.jgke.minpascal.exception.IdentifierAlreadyExists;
import fi.jgke.minpascal.exception.IdentifierNotFound;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class IdentifierContext {
    private static List<Map<String, CType>> identifiers = new ArrayList<>();
    private static List<Map<String, String>> realNames = new ArrayList<>();
    private static Stack<CType> functionContext = new Stack<>();
    private static int identifierNumber = 0;
    @Getter
    @Setter
    private static boolean lastStatementWasReturn = false;


    static {
        push();
        addIdentifier("true", CType.CBOOLEAN);
        addIdentifier("false", CType.CBOOLEAN);
    }

    public static CType getType(String identifier) {
        for (int i = identifiers.size() - 1; i >= 0; i--) {
            if (identifiers.get(i).containsKey(identifier.toLowerCase())) {
                return identifiers.get(i).get(identifier.toLowerCase());
            }
        }
        throw new IdentifierNotFound(identifier);
    }

    public static String getRealName(String identifier) {
        for (int i = realNames.size() - 1; i >= 0; i--) {
            if (realNames.get(i).containsKey(identifier.toLowerCase())) {
                return realNames.get(i).get(identifier.toLowerCase());
            }
        }
        throw new IdentifierNotFound(identifier);
    }

    public static void push() {
        //System.out.println("Push");
        identifiers.add(new HashMap<>());
        realNames.add(new HashMap<>());
    }

    public static void pop() {
        //System.out.println("Pop");
        identifiers.remove(identifiers.size() - 1);
        realNames.remove(realNames.size() - 1);
    }

    public static void addIdentifier(String identifier, CType type) {
        addIdentifier(identifier, identifier.toLowerCase(), type);
    }

    public static void addIdentifier(String identifier, String realName, CType type) {
        //System.out.println("Add " + identifier);
        if (identifiers.get(identifiers.size() - 1).containsKey(identifier.toLowerCase()))
            throw new IdentifierAlreadyExists(identifier);
        identifiers.get(identifiers.size() - 1).put(identifier.toLowerCase(), type);
        realNames.get(identifiers.size() - 1).put(identifier.toLowerCase(), realName);
    }

    public static String genIdentifier() {
        return genIdentifier("identifier");
    }

    public static String genIdentifier(String prefix) {
        return "_" + prefix + identifierNumber++;
    }

    public static CType getFunctionContext() {
        return functionContext.peek();
    }

    public static void pushFunctionContext(CType type) {
        functionContext.push(type);
    }

    public static void popFunctionContext() {
        functionContext.pop();
    }
}
