package fi.jgke.minpascal.compiler;

import fi.jgke.minpascal.astparser.AstParser;
import fi.jgke.minpascal.astparser.parsers.RuleMatch;
import fi.jgke.minpascal.compiler.std.CExpressionResult;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static fi.jgke.minpascal.compiler.CType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ExpressionResultTest {
    private double calc(double left, String op, double right) {
        if (op.equals("+"))
            return left + right;
        if (op.equals("-"))
            return left - right;
        if (op.equals("*"))
            return left * right;
        if (op.equals("/"))
            return left / right;
        throw new RuntimeException();
    }

    private void test(String exp, double expected, CType expectedType) {
        CExpressionResult expression = CExpressionResult
                .fromExpression(new RuleMatch("Expression").parse(exp).getLeft());
        Map<String, Double> vars = new HashMap<>();
        for (String s : expression.getTemporaries()) {
            String[] split = s.replaceAll(";", "").split(" ");
            String result = split[1];
            if (split.length == 4) {
                Double value = Double.parseDouble(split[3]);
                vars.put(result, value);
            } else if (split.length == 5) {
                vars.put(result, -vars.get(split[4]));
            } else {
                String left = split[3];
                String op = split[4];
                String right = split[5];
                vars.put(result, calc(vars.get(left), op, vars.get(right)));
            }
        }
        assertThat(vars.get(expression.getIdentifier()), is(equalTo(expected)));
        assertThat(expression.getType(), is(equalTo(expectedType)));
    }

    @Test
    public void testSimpleTypes() {
        AstParser.initDefaultParsers();
        test("1 + 1", 2, CINTEGER);
        test("1 + 2", 3, CINTEGER);
        test("-1 + 2", 1, CINTEGER);
        test("-1.5 + 2", 0.5, CDOUBLE);
        test("-4/2", -2, CINTEGER);
    }
}
