package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.TypeNode;
import fi.jgke.minpascal.parser.nodes.VarDeclarationNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class VarStatement implements Parsable {
    @Override
    public List<Parsable> getParsables() {
        return Collections.singletonList(VAR);
    }

    @Override
    public VarDeclarationNode parse(ParseQueue queue) {
        queue.getExpectedToken(VAR);
        ArrayList<Token<String>> identifiers = new ArrayList<>();
        //noinspection unchecked
        identifiers.add((Token<String>) queue.getIdentifier());
        while (queue.isNext(COMMA)) {
            queue.getExpectedToken(COMMA);
            //noinspection unchecked
            identifiers.add((Token<String>) queue.getIdentifier());
        }
        queue.getExpectedToken(COLON);
        TypeNode type = new Type().parse(queue);
        return new VarDeclarationNode(identifiers, type);
    }
}
