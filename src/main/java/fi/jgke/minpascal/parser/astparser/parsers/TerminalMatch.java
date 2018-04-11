package fi.jgke.minpascal.parser.astparser.parsers;

import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.astparser.Regex;
import fi.jgke.minpascal.parser.astparser.nodes.AstNode;
import fi.jgke.minpascal.parser.astparser.nodes.LeafNode;
import fi.jgke.minpascal.util.Pair;

import java.util.List;

public class TerminalMatch implements Parser {
    private final String name;
    private final Regex match;

    public TerminalMatch(String name, String match, boolean regex) {
        if (!regex)
            match = Regex.quote(match);
        this.name = name;
        this.match = new Regex('(' + match + ')');
    }

    @Override
    public Pair<AstNode, String> parse(String str) {
        List<String> groups;
        try {
            groups = match.getGroups(str);
        } catch (IllegalStateException e) {
            System.out.println("No match when parsing " + name + " (" + match + ')');
            System.out.println(str);
            throw new CompilerException(e);
        }
        return new Pair<>(new LeafNode(name, groups.get(1)), str.substring(groups.get(0).length()));
    }

    @Override
    public boolean parses(String str) {
        return match.matches(str);
    }

    @Override
    public String toString() {
        return match.toString();
    }
}
