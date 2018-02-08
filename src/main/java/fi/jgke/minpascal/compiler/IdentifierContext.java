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

    public static CType getType(String identifier) {
        for (int i = identifiers.size() - 1; i >= 0; i--) {
            if (identifiers.get(i).containsKey(identifier)) {
                return identifiers.get(i).get(identifier);
            }
        }
        throw new IdentifierNotFound(identifier);
    }

    public static void push() {
        identifiers.add(new HashMap<>());
    }

    public static void pop() {
        identifiers.remove(identifiers.size()-1);
    }

    public static void addIdentifier(String identifier, CType type) {
        if(identifiers.get(identifiers.size()-1).containsKey(identifier))
            throw new IdentifierAlreadyExists(identifier);
        identifiers.get(identifiers.size()-1).put(identifier, type);
    }

    public static String genIdentifier() {
        return "_identifier" + identifierNumber++;
    }
}
