package fi.jgke.minpascal.parser.expressions;

import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;

import java.util.Arrays;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.MINUS;
import static fi.jgke.minpascal.data.TokenType.PLUS;

public class SimpleExpression implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(PLUS, MINUS, new Term());
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        return null;
    }
}
