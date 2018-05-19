package fi.jgke.minpascal.compiler;

import fi.jgke.minpascal.astparser.AstParser;
import fi.jgke.minpascal.astparser.parsers.RuleParser;
import fi.jgke.minpascal.compiler.std.CBinaryExpressions;
import fi.jgke.minpascal.compiler.std.CExpressionResult;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.exception.OperatorError;
import fi.jgke.minpascal.exception.TypeError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fi.jgke.minpascal.compiler.CType.CDOUBLE;
import static fi.jgke.minpascal.compiler.CType.CINTEGER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ExpressionResultTest {
    private final Position p = new Position(0, 0);

    @Before
    public void init() {
        AstParser.initDefaultParsers();
        IdentifierContext.push();
    }

    @After
    public void teardown() {
        IdentifierContext.pop();
    }

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
        test(exp, expected, expectedType, new HashMap<>());
    }

    private CExpressionResult getExpression(String exp) {
        return CExpressionResult.fromExpression(new RuleParser("Expression").parse(exp).getLeft());
    }

    private void test(String exp, double expected, CType expectedType, Map<String, Function<String, Double>> fnCalls) {
        CExpressionResult expression = getExpression(exp);
        Map<String, Double> vars = new HashMap<>();
        for (String s : expression.getTemporaries()) {
            String[] split = s.replaceAll(";", "").split(" ");
            String result = split[1];
            if (split.length == 4) {
                Pattern fnCall = Pattern.compile("^([a-z0-9]+)\\(([a-z0-9,_-]*)\\)$");
                Matcher fnMatch = fnCall.matcher(split[3]);
                Pattern arrIndex = Pattern.compile("^([a-z0-9]+)\\[([a-z0-9_-]*)]$");
                Matcher arrMatch = arrIndex.matcher(split[3]);
                if (fnMatch.matches()) {
                    split[3] = "" + fnCalls.get(fnMatch.group(1)).apply(fnMatch.group(2));
                } else if (arrMatch.matches()) {
                    String group = arrMatch.group(2);
                    split[3] = "" + fnCalls.get(arrMatch.group(1)).apply(vars.containsKey(group) ? "" + vars.get(group) : group);
                }
                Double value = Double.parseDouble(split[3]);
                vars.put(result, value);
            } else if (split.length == 5) {
                if (split[3].equals("-"))
                    vars.put(result, -vars.get(split[4]));
                else
                    vars.put(result, vars.get(split[4]));
            } else if (split.length == 3) {
                Double value = Double.parseDouble(split[2]);
                vars.put(result, value);
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
        test("1 + 1", 2, CINTEGER);
        test("+1 + 1", 2, CINTEGER);
        test("1 + 2", 3, CINTEGER);
        test("-1 + 2", 1, CINTEGER);
        test("-1.5 + 2", 0.5, CDOUBLE);
        test("-4/2", -2, CINTEGER);
    }

    @Test(expected = TypeError.class)
    public void negativeBoolean() {
        getExpression("-true");
    }

    @Test
    public void functionCalls() {
        IdentifierContext.addIdentifier("foo1", new CType(CType.CINTEGER, Collections.emptyList()), p);
        IdentifierContext.addIdentifier("foo2", new CType(CType.CINTEGER, Collections.singletonList(CType.CINTEGER)), p);
        IdentifierContext.addIdentifier("foo3", new CType(CType.CINTEGER, Arrays.asList(CType.CINTEGER, CType.CINTEGER)), p);
        IdentifierContext.addIdentifier("foo4", new CType(CType.CINTEGER, Arrays.asList(CType.CINTEGER, CType.CINTEGER, CType.CINTEGER)), p);
        test("foo1()", 1, CINTEGER, Collections.singletonMap("foo1", $ -> 1.0));
        test("foo2(5)", 2, CINTEGER, Collections.singletonMap("foo2", $ -> 2.0));
        test("foo3(5, 3)", 3, CINTEGER, Collections.singletonMap("foo3", $ -> 3.0));
        test("foo4(5, 5, 1)", 4, CINTEGER, Collections.singletonMap("foo4", $ -> 4.0));
    }

    @Test
    public void arrays() {
        IdentifierContext.addIdentifier("foo", CType.ptrTo(CType.CINTEGER), p);
        IdentifierContext.addIdentifier("qux", CType.ptrTo(new CType(CType.CINTEGER, Collections.emptyList())), p);
        IdentifierContext.addIdentifier("bar", CType.CINTEGER, p);
        test("foo[0]", 1, CINTEGER, Collections.singletonMap("foo", i -> Double.parseDouble(i) + 1));
        test("foo.size", 0, CINTEGER, Collections.singletonMap("foo", i -> Double.parseDouble(i) + 1));
        CExpressionResult expression = getExpression("qux[0]()");
        assertThat("Array of functions works",
                expression.getTemporaries().get(2).matches("^int \\(\\*_identifier[0-9]+\\)\\(\\) = qux\\[_identifier[0-9]+];$"));
        assertThat("Array of functions works",
                expression.getTemporaries().get(3).matches("^int _identifier[0-9]+ = _identifier[0-9]+\\(\\);$"));
    }

    private void opTest(String expr, String expected) {
        CExpressionResult expression = getExpression(expr);
        expected = ".* = " + expected + ";";
        if (expression.getTemporaries().size() == 1) {
            assertThat("Relative expression matches expected form (expected: " + expected + ", got: " + expression.getTemporaries().get(0) + ")",
                    expression.getTemporaries().get(0).matches(expected));
        } else {
            assertThat(expression.getTemporaries().size(), is(equalTo(3)));
            assertThat("Relative expression matches expected form (expected: " + expected + ", got: " + expression.getTemporaries().get(0) + ")",
                    expression.getTemporaries().get(2).matches(expected));
        }
    }

    @Test
    public void operatorTests() {
        opTest("1 < 2", "_identifier[0-9]+ < _identifier[0-9]+");
        opTest("1 <= 2", "_identifier[0-9]+ <= _identifier[0-9]+");
        opTest("1 == 2", "_identifier[0-9]+ == _identifier[0-9]+");
        opTest("1 <> 2", "_identifier[0-9]+ != _identifier[0-9]+");
        opTest("1 > 2", "_identifier[0-9]+ > _identifier[0-9]+");
        opTest("1 >= 2", "_identifier[0-9]+ >= _identifier[0-9]+");

        opTest("1 + 2", "_identifier[0-9]+ \\+ _identifier[0-9]+");
        opTest("1 - 2", "_identifier[0-9]+ - _identifier[0-9]+");
        opTest("true or false", "true \\|\\| false");

        opTest("1 * 2", "_identifier[0-9]+ \\* _identifier[0-9]+");
        opTest("1 / 2", "_identifier[0-9]+ / _identifier[0-9]+");
        opTest("1 % 2", "_identifier[0-9]+ % _identifier[0-9]+");
        opTest("true and false", "true && false");
    }

    @Test
    public void ptrTests() {
        IdentifierContext.addIdentifier("a", CType.CINTEGER, p);
        IdentifierContext.addIdentifier("b", CType.ptrTo(CType.CINTEGER), p);
        IdentifierContext.addIdentifier("c", new CType(CType.CINTEGER, Collections.singletonList(CType.CINTEGER)), p);
        IdentifierContext.addIdentifier("d", new CType(CType.CINTEGER, Collections.singletonList(CType.ptrTo(CType.CINTEGER))), p);
        opTest("c(a)", "c\\(a\\)");
        opTest("c(b)", "c\\(\\*b\\)");
        opTest("d(a)", "d\\(&a\\)");
        opTest("d(b)", "d\\(b\\)");
    }

    @Test(expected = TypeError.class)
    public void invalidDereference() {
        IdentifierContext.addIdentifier("a", CType.CINTEGER, p);
        getExpression("a[0]");
    }

    @Test(expected = TypeError.class)
    public void invalidSizeof() {
        IdentifierContext.addIdentifier("a", CType.CINTEGER, p);
        getExpression("a.size");
    }

    @Test(expected = TypeError.class)
    public void invalidFunctionCall() {
        IdentifierContext.addIdentifier("a", CType.CINTEGER, p);
        getExpression("a()");
    }

    @Test(expected = TypeError.class)
    public void invalidFunctionParameters() {
        IdentifierContext.addIdentifier("a", new CType(CType.CINTEGER, Collections.singletonList(CType.ptrTo(CType.CINTEGER))), p);
        getExpression("a()");
    }

    @Test(expected = TypeError.class)
    public void invalidFunctionParameterType() {
        IdentifierContext.addIdentifier("a", new CType(CType.CINTEGER, Collections.singletonList(CType.ptrTo(CType.CINTEGER))), p);
        getExpression("a(\"foo\")");
    }

    @Test
    public void parenExpression() {
        getExpression("1 + (2 + 3)");
    }

    @Test
    public void notExpression() {
        getExpression("!true");
    }

    @Test(expected = TypeError.class)
    public void notInteger() {
        getExpression("!1");
    }

    @Test(expected = OperatorError.class)
    public void trueAndInteger() {
        getExpression("true and 1");
    }

    @Test(expected = OperatorError.class)
    public void integerOrFalse() {
        getExpression("1 or false");
    }

    @Test(expected = OperatorError.class)
    public void functionPlus() {
        IdentifierContext.addIdentifier("foo", new CType(CType.CINTEGER, Collections.emptyList()), p);
        CBinaryExpressions.apply(getExpression("foo"), TokenType.PLUS, getExpression("true"), p);
    }

    @Test
    public void ptrPlusPtr() {
        IdentifierContext.addIdentifier("a", CType.ptrTo(CINTEGER), p);
        IdentifierContext.addIdentifier("b", CType.ptrTo(CINTEGER), p);
        CBinaryExpressions.apply(getExpression("a"), TokenType.PLUS, getExpression("b"), p);
    }
}
