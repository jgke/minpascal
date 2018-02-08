package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.Configuration;
import fi.jgke.minpascal.data.Token;
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
        if (Configuration.STRICT_MODE) {
            return Arrays.asList(INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL);
        }
        return Arrays.asList(INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL, TRUE, FALSE);
    }

    @Override
    public LiteralNode parse(ParseQueue queue) {
        Token token;
        if (Configuration.STRICT_MODE) {
            token = queue.getExpectedToken(INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL);
        } else {
            token = queue.getExpectedToken(INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL, TRUE, FALSE);
        }

        if (Arrays.asList(INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL).contains(token.getType())) {
            Object content = token.getValue().orElse(null);
            Integer integer = null;
            Double number = null;
            String string = null;
            Boolean bool = null;

            if (token.getType().equals(INTEGER_LITERAL)) integer = (int) content;
            else if (token.getType().equals(REAL_LITERAL)) number = (double) content;
            else if (token.getType().equals(STRING_LITERAL)) string = (String) content;

            assert integer != null || number != null || string != null;

            return new LiteralNode(
                    Optional.ofNullable(integer),
                    Optional.ofNullable(number),
                    Optional.ofNullable(string),
                    Optional.empty()
            );
        }

        Boolean bool = null;

        if (TRUE.matches(token)) bool = true;
        else if (FALSE.matches(token)) bool = false;

        assert bool != null;
        return new LiteralNode(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(bool)
        );
    }
}
