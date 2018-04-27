package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.data.TokenType;

import java.util.HashMap;

import static fi.jgke.minpascal.data.TokenType.*;

public class CBinaryExpressionFnPrivate {
    private static final HashMap<String, TokenType> operators;
    private static final HashMap<TokenType, String> format;

    static {
        operators = new HashMap<>();
        format = new HashMap<>();

        operators.put("+", PLUS);
        operators.put("-", MINUS);
        operators.put("*", TIMES);
        operators.put("/", DIVIDE);
        operators.put("%", MOD);
        operators.put("==", EQUALS);
        operators.put("<>", NOTEQUALS);
        operators.put("<", LESSTHAN);
        operators.put(">", MORETHAN);
        operators.put("<=", LESSTHANEQUALS);
        operators.put(">=", MORETHANEQUALS);
        operators.put("or", OR);
        operators.put("and", AND);
        operators.put("not", NOT);

        format.put(PLUS, "+");
        format.put(MINUS, "-");
        format.put(TIMES, "*");
        format.put(DIVIDE, "/");
        format.put(MOD, "%");
        format.put(EQUALS, "==");
        format.put(NOTEQUALS, "!=");
        format.put(LESSTHAN, "<");
        format.put(MORETHAN, ">");
        format.put(LESSTHANEQUALS, "<=");
        format.put(MORETHANEQUALS, ">=");
        format.put(OR, "||");
        format.put(AND, "&&");
        format.put(NOT, "!");
    }

    public static String getFormat(TokenType op) {
        return format.get(op);
    }

    public static TokenType getOperator(String op) {
        return operators.get(op);
    }
}
