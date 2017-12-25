package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.ParametersNode;
import fi.jgke.minpascal.parser.nodes.TypeNode;
import fi.jgke.minpascal.parser.nodes.VarDeclarationNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class Parameters implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(CLOSEPAREN, VAR, IDENTIFIER);
    }

    @Override
    public ParametersNode parse(ParseQueue queue) {
        List<VarDeclarationNode> declarations = queue.collectByComma(q -> {
            queue.ifNextConsume(VAR);
            Token identifier = queue.getExpectedToken(IDENTIFIER);
            queue.getExpectedToken(COLON);
            TypeNode type = new Type().parse(queue);
            return new VarDeclarationNode(Collections.singletonList(identifier), type);
        });

        return new ParametersNode(declarations);
    }
}
