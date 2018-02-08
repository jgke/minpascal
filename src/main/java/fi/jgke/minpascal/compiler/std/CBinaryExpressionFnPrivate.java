package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.data.TokenType;

import java.util.HashMap;

import static fi.jgke.minpascal.data.TokenType.*;

public class CBinaryExpressionFnPrivate {
    private static final HashMap<TokenType, String> operators;
    static {
        operators = new HashMap<>();
        operators.put(PLUS, "+");
        operators.put(MINUS, "-");
        operators.put(TIMES, "*");
        operators.put(DIVIDE, "/");
        operators.put(MOD, "%");
        operators.put(EQUALS, "==");
        operators.put(NOTEQUALS, "!=");
        operators.put(LESSTHAN, "<");
        operators.put(MORETHAN, ">");
        operators.put(LESSTHANEQUALS, "<=");
        operators.put(MORETHANEQUALS, ">=");
        operators.put(OR, "||");
        operators.put(AND, "&&");
        operators.put(NOT, "!");
    }

    public static String getOperator(TokenType op) {
        return operators.get(op);
    }
}
