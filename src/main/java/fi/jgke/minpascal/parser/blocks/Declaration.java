package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.DeclarationNode;
import fi.jgke.minpascal.parser.nodes.TypeNode;
import fi.jgke.minpascal.parser.nodes.VarDeclarationNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.jgke.minpascal.data.TokenType.*;

public class Declaration implements Parsable {
    private final Parsable[] children = new Parsable[]{
            new VarStatement(), new ProcedureStatement(), new FunctionStatement()
    };

    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(children);
    }

    private class VarStatement implements Parsable {
        @Override
        public List<Parsable> getParsables() {
            return Collections.singletonList(VAR);
        }

        @Override
        public VarDeclarationNode parse(ParseQueue queue) {
            queue.getExpectedToken(VAR);
            ArrayList<Token> identifiers = new ArrayList<>();
            identifiers.add(queue.getExpectedToken(IDENTIFIER));
            while (queue.isNext(COMMA)) {
                queue.getExpectedToken(COMMA);
                identifiers.add(queue.getExpectedToken(IDENTIFIER));
            }
            queue.getExpectedToken(COLON);
            TypeNode type = new Type().parse(queue);
            return new VarDeclarationNode(identifiers, type);
        }
    }

    @Override
    public DeclarationNode parse(ParseQueue queue) {
        return new DeclarationNode(queue.any(children));
    }
}
