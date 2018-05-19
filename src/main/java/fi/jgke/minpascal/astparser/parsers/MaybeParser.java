package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.EmptyNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
public class MaybeParser implements Parser {
    @Getter
    private final String name;
    private final Parser maybeThis;
    private final Parser yesThis;

    @Override
    public Pair<AstNode, Pair<String, Position>> parse(Pair<String, Position> str) {
        Position p = str.getRight();
        AstNode inner = new EmptyNode(maybeThis.getName(), str.getRight());
        if (maybeThis.parses(str.getLeft())) {
            Pair<AstNode, Pair<String, Position>> parse = maybeThis.parse(str);
            str = parse.getRight();
            inner = parse.getLeft();
        } else if (yesThis instanceof Epsilon) {
            return new Pair<>(new EmptyNode(name, p), str);
        }
        Pair<AstNode, Pair<String, Position>> parse = yesThis.parse(str);
        return new Pair<>(new ListAstNode(name, Arrays.asList(inner, parse.getLeft()), p), parse.getRight());
    }

    @Override
    public boolean parses(String str) {
        return maybeThis.parses(str) || yesThis.parses(str);
    }

    @Override
    public String toString() {
        return "[" + maybeThis + "]: " + yesThis;
    }
}
