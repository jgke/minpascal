package fi.jgke.minpascal.parser.statements;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static fi.jgke.minpascal.data.TokenType.IDENTIFIER;

/*
 * Original syntax is not LL(1):
 * <assignment statement> | <call> | <return statement> | <read statement> | <write statement> | <assert statement>
 * so rewrite it to LL(1):
 * <identifier> <identifier statement> | <return statement> | <read statement> | <write statement> | <assert statement>
 * <identifier statement := <assignment statement> | <call>
 */

public class SimpleStatement implements Parsable {
    private static final Parsable[] children = new Parsable[]{
            new Read(),
            new Write(),
            new IdentifierStatement(),
            new Return(),
            new Assert()
    };

    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(children);
    }

    @Override
    public SimpleStatementNode parse(ParseQueue queue) {
        ReturnNode rn = null;
        ReadNode read = null;
        WriteNode write = null;
        AssertNode an = null;
        CallNode call = null;
        AssignmentNode asn = null;

        TreeNode content = queue.any(children);

        if (content instanceof ReturnNode) rn = (ReturnNode) content;
        else if (content instanceof ReadNode) read = (ReadNode) content;
        else if (content instanceof WriteNode) write = (WriteNode) content;
        else if (content instanceof AssertNode) an = (AssertNode) content;
        else if (content instanceof CallNode) call = (CallNode) content;
        else asn = (AssignmentNode) content;

        return new SimpleStatementNode(
                Optional.ofNullable(rn),
                Optional.ofNullable(read),
                Optional.ofNullable(write),
                Optional.ofNullable(an),
                Optional.ofNullable(call),
                Optional.ofNullable(asn)
        );
    }

    private static class IdentifierStatement implements Parsable {
        @Override
        public List<Parsable> getParsables() {
            return Collections.singletonList(IDENTIFIER);
        }

        @Override
        public TreeNode parse(ParseQueue queue) {
           Token<String> identifier = queue.getIdentifier();
            if (new Call().matches(queue))
                return new Call().parseWithIdentifier(identifier, queue);
            return new AssignmentStatement().parseWithIdentifier(identifier, queue);
        }
    }
}
