package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.util.Pair;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NotMatch implements Parser {
    private final Parser notThis;
    private final Parser yesThis;

    @Override
    public Pair<AstNode, Pair<String, Position>> parse(Pair<String, Position> str) {
        if (notThis.parses(str.getLeft()))
            throw new CompilerException("NotMatch.parse called when notThis matches");
        return yesThis.parse(str);
    }

    public String getName() {
        return yesThis.getName();
    }

    @Override
    public boolean parses(String str) {
        return !notThis.parses(str) && yesThis.parses(str);
    }
}
