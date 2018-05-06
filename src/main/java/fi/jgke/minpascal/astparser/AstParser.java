package fi.jgke.minpascal.astparser;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.parsers.Parser;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
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
                rules = Arrays.stream(bufferedReader.lines().filter(s -> !s.startsWith("//")).collect(Collectors.joining("\n"))
                        .replaceAll("\n(?![^ ])", " ").split("\n"))
                        .filter(rule -> !rule.isEmpty())
                        .map(Rule::new)
                        .collect(Collectors.toMap(Rule::get_name, Rule::getParser));
            } catch (IOException | NullPointerException e) {
                throw new CompilerException(e);
            }
        } else {
            throw new CompilerException("Couldn't open grammar");
        }
    }

    public static AstNode parse(String s) {
        Pair<AstNode, String> parse = rules.get("S").parse(s.replaceAll("\n", " "));

        if (!parse.getRight().trim().isEmpty()) {
            throw new CompilerException("Input not empty after parsing");
        }
        //System.out.println(formatTree(parse.getLeft().toString()));
        //System.out.println(parse.getLeft().toString());
        return parse.getLeft();
    }
}