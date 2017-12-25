package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.expressions.Expression;
import fi.jgke.minpascal.parser.nodes.ArrayTypeNode;
import fi.jgke.minpascal.parser.nodes.ExpressionNode;
import fi.jgke.minpascal.parser.nodes.SimpleTypeNode;

import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class ArrayType implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(ARRAY);
    }

    @Override
    public TreeNode parse(ParseQueue queue) {
        queue.getExpectedTokens(ARRAY, OPENBRACKET);
        ExpressionNode integerExpression = new Expression().parse(queue);
        queue.getExpectedTokens(CLOSEBRACKET, OF);
        SimpleTypeNode type = new SimpleType().parse(queue);
        return new ArrayTypeNode(integerExpression, type);
    }
}
