package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.LiteralNode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static fi.jgke.minpascal.data.TokenType.*;

public class Literal implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL);
    }

    @Override
    public LiteralNode parse(ParseQueue queue) {
        Token token = queue.getExpectedToken(INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL);
        Object content = token.getValue()
                .orElseThrow(() -> new CompilerException("Literal token didn't have a value"));
        Integer integer = null;
        Double number = null;
        String string = null;

        if (token.getType().equals(INTEGER_LITERAL)) integer = (Integer) content;
        else if (token.getType().equals(INTEGER_LITERAL)) number = (Double) content;
        else if (token.getType().equals(INTEGER_LITERAL)) string = (String) content;

        return new LiteralNode(
                Optional.ofNullable(integer),
                Optional.ofNullable(number),
                Optional.ofNullable(string)
        );
    }
}
