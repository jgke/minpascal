package fi.jgke.minpascal.compiler;

import fi.jgke.minpascal.exception.IdentifierAlreadyExists;
import fi.jgke.minpascal.exception.IdentifierNotFound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentifierContext {
    private static List<Map<String, CType>> identifiers = new ArrayList<>();
    private static int identifierNumber = 0;

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

    public static void push() {
        //System.out.println("Push");
        identifiers.add(new HashMap<>());
    }

    public static void pop() {
        //System.out.println("Pop");
        identifiers.remove(identifiers.size()-1);
    }

    public static void addIdentifier(String identifier, CType type) {
        //System.out.println("Add " + identifier);
        if(identifiers.get(identifiers.size()-1).containsKey(identifier.toLowerCase()))
            throw new IdentifierAlreadyExists(identifier);
        identifiers.get(identifiers.size()-1).put(identifier.toLowerCase(), type);
    }

    public static String genIdentifier() {
        return genIdentifier("identifier");
    }
    public static String genIdentifier(String prefix) {
        return "_" + prefix + identifierNumber++;
    }
}
