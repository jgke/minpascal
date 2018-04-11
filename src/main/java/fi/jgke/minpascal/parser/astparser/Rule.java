package fi.jgke.minpascal.parser.astparser;

import fi.jgke.minpascal.parser.astparser.parsers.*;
import lombok.Data;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Rule {
    private final String name;
    private final String pattern;

    public Rule(String rule) {
        List<String> groups = new Regex("([a-zA-Z_]+) ::= (.*)").getGroups(rule);
        name = groups.get(1);
        pattern = groups.get(2);
    }

    /* split + Intersperse */
    private static List<String> flatmapSplit(List<String> strs, String delimiter) {
        return strs.stream()
                .map(str -> Arrays.stream(str.split(Pattern.quote(delimiter), -1)))
                .flatMap(ss -> ss.flatMap(s1 -> Stream.of(delimiter, s1)).skip(1))
                .filter(s -> !s.trim().isEmpty())
                .collect(Collectors.toList());
    }

    private Parser toJust(List<Parser> parsers) {
        if (parsers.size() == 1)
            return parsers.get(0);
        return new JustMatch(parsers, name);
    }

    private Parser getParser(Queue<String> tokens) {
        List<Parser> parsers = new ArrayList<>();
        loop:
        while (!tokens.isEmpty()) {
            String remove = tokens.remove();
            switch (remove) {
                case "[":
                    String myName = tokens.peek();
                    Parser maybeContent = getParser(tokens);
                    Parser yesContent;
                    yesContent = isTerminating(tokens)
                            ? new Epsilon()
                            : getParser(tokens);
                    parsers.add(new MaybeMatch(myName, maybeContent, yesContent));
                    break;
                case "(":
                    parsers.add(getParser(tokens));
                    break;
                case "!":
                    String key = tokens.remove();
                    parsers.add(new NotMatch(new RuleMatch(key), getParser(tokens)));
                    break;
                case "*":
                    Parser parser = parsers.get(parsers.size() - 1);
                    parsers.remove(parsers.size() - 1);
                    parsers.add(new KleeneMatch(parser));
                    break;
                case "|":
                    Parser inner = toJust(parsers);
                    parsers = new ArrayList<>();
                    parsers.add(new OrMatch(Arrays.asList(inner, getParser(tokens))));
                    break;
                case "]":
                    break loop;
                case ")":
                    break loop;
                default:
                    parsers.add(new RuleMatch(remove));
            }
        }
        return toJust(parsers);
    }

    private boolean isTerminating(Queue<String> tokens) {
        return tokens.isEmpty() || tokens.peek().equals(")") || tokens.peek().equals("|") || tokens.peek().equals("]");
    }

    public Parser getParser() {
        Regex strRegex = new Regex("^\".*\"$");
        Regex regexRegex = new Regex("^#\".*\"$");
        if (strRegex.matches(pattern)) {
            return new TerminalMatch(name, pattern.substring(1, pattern.length() - 1), false);
        } else if (regexRegex.matches(pattern)) {
            return new TerminalMatch(name, pattern.substring(2, pattern.length() - 1), true);
        }
        List<String> split = Arrays.asList(pattern.split("\\s+"));
        for (String s : Arrays.asList("!", "[", "]", "|", "(", ")", "*")) {
            split = flatmapSplit(split, s);
        }
        return getParser(new ArrayDeque<>(split));
    }

}
