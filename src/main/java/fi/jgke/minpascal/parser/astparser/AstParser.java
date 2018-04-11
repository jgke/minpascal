package fi.jgke.minpascal.parser.astparser;

import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.astparser.nodes.AstNode;
import fi.jgke.minpascal.parser.astparser.parsers.Parser;
import fi.jgke.minpascal.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

/* Parse the BNF */
public class AstParser {
    public static final Map<String, Parser> rules;

    static {
        URL resource = AstParser.class.getClassLoader().getResource("MinPascal.bnf");
        if (resource != null) {
            try (InputStream inputStream = resource.openStream();
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                rules = bufferedReader
                        .lines()
                        .filter(rule -> !rule.isEmpty())
                        .map(Rule::new)
                        .collect(Collectors.toMap(Rule::getName, Rule::getParser));
            } catch (IOException | NullPointerException e) {
                throw new CompilerException(e);
            }
        } else {
            throw new CompilerException("Couldn't open grammar");
        }
    }

    public static void parse(String s) {
        Pair<AstNode, String> parse = rules.get("S").parse(s.replaceAll("\n", " "));

        if (!parse.getRight().trim().isEmpty()) {
            throw new CompilerException("Input not empty after parsing");
        }
    }
}
