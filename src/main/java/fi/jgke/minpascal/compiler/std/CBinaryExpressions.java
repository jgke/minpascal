package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.exception.CompilerException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static fi.jgke.minpascal.compiler.CType.*;
import static fi.jgke.minpascal.data.TokenType.*;

public class CBinaryExpressions {
    private static final Map<CType, Map<TokenType, Map<CType, CBinaryExpressionFn>>> nest;

    static {
        nest = new HashMap<>();
        // Running these is order-dependent - we want str + anything to be str and use the str function for them
        // but use the standard addition for ints and so on, so first do str for all and override with std
        binary(CBinaryExpressionFn::str, new CType[]{CSTRING, CINTEGER, CDOUBLE}, PLUS);
        binary(CBinaryExpressionFn::str, new CType[]{CSTRING, CBOOLEAN}, PLUS);
        binary(CBinaryExpressionFn::std, new CType[]{CBOOLEAN}, AND, OR, EQUALS);
        binary(CBinaryExpressionFn::std, new CType[]{CINTEGER, CDOUBLE}, PLUS, MINUS, TIMES, DIVIDE, MOD);
        nest.get(CBOOLEAN).remove(PLUS);
    }

    private static CType max(CType a, CType b) {
        if (a.equals(CSTRING) || b.equals(CSTRING)) {
            return CSTRING;
        } else if (a.equals(CBOOLEAN) || b.equals(CBOOLEAN)) {
            if (a.equals(CBOOLEAN) && b.equals(CBOOLEAN)) {
                return CBOOLEAN;
            }
            CType got = a;
            if (a.equals(CBOOLEAN)) {
                got = b;
            }
            throw new CompilerException("Expected CBOOLEAN, got " + got);
        } else if (a.equals(CDOUBLE) || b.equals(CDOUBLE)) {
            return CDOUBLE;
        }
        return CINTEGER;
    }

    private static void add(CType left, TokenType operator, CType right, CBinaryExpressionFn result) {
        nest.putIfAbsent(left, new HashMap<>());
        nest.get(left).putIfAbsent(operator, new HashMap<>());
        nest.get(left).get(operator).put(right, result);
    }

    private static void binary(Function<CType, CBinaryExpressionFn> supplier, CType[] type, TokenType... operators) {
        Arrays.stream(type).forEach(a ->
                Arrays.stream(type).forEach(b ->
                        Arrays.stream(operators).forEach(operator -> add(a, operator, b, supplier.apply(max(a, b))))
                )
        );
    }

    public static CExpressionResult apply(CExpressionResult left, Token<Void> operator, CExpressionResult right) {
        return nest.get(left.getType())
                .get(operator.getType())
                .get(right.getType())
                .apply(left, operator, right);
    }
}
