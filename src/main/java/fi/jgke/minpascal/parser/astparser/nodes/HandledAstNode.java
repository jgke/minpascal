package fi.jgke.minpascal.parser.astparser.nodes;

import lombok.ToString;

@ToString
public class HandledAstNode implements AstNode {
    public static final HandledAstNode instance = new HandledAstNode();

    @Override
    public void error() {
    }
}
