package fi.jgke.minpascal.astparser.parsers;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.util.Pair;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class AndMatch implements Parser {
    @Getter
    private final List<Parser> parsers;
    private final String name;
    private static final Parser whitespace = new RuleMatch("whitespace");
    private final boolean noWhitespace;

    public AndMatch(List<Parser> parsers, String name, boolean noWhitespace) {
        this.parsers = parsers;
        this.name = name;
        this.noWhitespace = noWhitespace;
    }

    public String getName() {
        return name;
    }

    @Override
    public Pair<AstNode, String> parse(String str) {
        List<AstNode> nodes = new ArrayList<>();
        for (Parser p : parsers) {
            if(!noWhitespace) {
                str = whitespace.parse(str).getRight();
            }
            Pair<AstNode, String> pair = p.parse(str);
            nodes.add(pair.getLeft());
            str = pair.getRight();
        }
        if(Character.isLowerCase(name.charAt(0)) && nodes.size() == 1) {
            return new Pair<>(nodes.get(0), str);
        }
        return new Pair<>(new ListAstNode(name, nodes), str);
    }

    @Override
    public boolean parses(String str) {
        return parsers.get(0).parses(str);
    }

    @Override
    public String toString() {
        String s = parsers.toString();
        return name + '{' + s.substring(1, s.length() - 1) + '}';
    }
}
