package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.DeclarationNode;
import fi.jgke.minpascal.parser.nodes.SimpleStatementNode;
import fi.jgke.minpascal.parser.nodes.StatementNode;
import fi.jgke.minpascal.parser.nodes.StructuredStatementNode;
import fi.jgke.minpascal.parser.statements.SimpleStatement;
import fi.jgke.minpascal.parser.statements.StructuredStatement;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Statement implements Parsable {
    private final SimpleStatement simpleStatement = new SimpleStatement();
    private final StructuredStatement structuredStatement = new StructuredStatement();
    private final Declaration declaration = new Declaration();

    private final Parsable[] children = new Parsable[]{
            simpleStatement, structuredStatement, declaration
    };

    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(children);
    }

    @Override
    public StatementNode parse(ParseQueue queue) {
        SimpleStatementNode simple = null;
        StructuredStatementNode structured = null;
        DeclarationNode declarationNode = null;

        if (simpleStatement.matches(queue)) simple = simpleStatement.parse(queue);
        else if (structuredStatement.matches(queue)) structured = structuredStatement.parse(queue);
        else if (declaration.matches(queue)) declarationNode = declaration.parse(queue);

        return new StatementNode(
                Optional.ofNullable(simple),
                Optional.ofNullable(structured),
                Optional.ofNullable(declarationNode)
        );
    }
}
