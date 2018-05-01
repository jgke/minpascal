package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.EmptyNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
public class MaybeMatch implements Parser {
    @Getter
    private final String name;
    private final Parser maybeThis;
    private final Parser yesThis;

    @Override
    public Pair<AstNode, String> parse(String str) {
        AstNode inner = new EmptyNode(name);
        if (maybeThis.parses(str)) {
            Pair<AstNode, String> parse = maybeThis.parse(str);
            str = parse.getRight();
            inner = parse.getLeft();
        } else if (yesThis instanceof Epsilon) {
            return new Pair<>(new EmptyNode(name), str);
        }
        Pair<AstNode, String> parse = yesThis.parse(str);
        return new Pair<>(new ListAstNode(name, Arrays.asList(inner, parse.getLeft())), parse.getRight());
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
