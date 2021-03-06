package fi.jgke.minpascal.astparser;

import fi.jgke.minpascal.astparser.parsers.*;
import fi.jgke.minpascal.util.Regex;
import lombok.Data;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Rule {
    private final String _name;
    private final String pattern;

    public Rule(String rule) {
        String[] split = rule.split(" ::= ");
        _name = split[0];
        pattern = split[1];
    }

    /* split + Intersperse */
    // eg. tokenize(tokenize(Arrays.asList("15+23*2+32*12+5"), "*"), "+"
    // -> ["15", "+", "23", "*", "2", "+", "32", "*", "12", "+", "5"]
    private static List<String> tokenize(List<String> strs, String delimiter) {
        return strs.stream()
                .map(str -> Arrays.stream(str.split(Pattern.quote(delimiter), -1)))
                .flatMap(ss -> ss
                        .flatMap(s1 -> Stream.of(delimiter, s1))
                        .skip(1))
                .filter(s -> !s.trim().isEmpty())
                .collect(Collectors.toList());
    }

    private Parser toJust(List<Parser> parsers, String name) {
        if (parsers.size() <= 1)
            return parsers.get(0);
        return new AndParser(name, parsers);
    }

    private Parser getParser(Queue<String> tokens, String name) {
        List<Parser> parsers = new ArrayList<>();
        loop:
        while (!tokens.isEmpty()) {
            String remove = tokens.remove();
            switch (remove) {
                case "[":
                    String myName = tokens.peek();
                    Parser maybeContent = getParser(tokens, myName);
                    Parser yesContent;
                    yesContent = isTerminating(tokens)
                            ? new Epsilon()
                            : getParser(tokens, tokens.peek());
                    parsers.add(new MaybeParser(myName, maybeContent, yesContent));
                    break;
                case "(":
                    parsers.add(getParser(tokens, tokens.peek()));
                    break;
                case "!":
                    String key = tokens.remove();
                    parsers.add(new NotParser(new RuleParser(key), getParser(tokens, tokens.peek())));
                    break;
                case "*":
                    Parser parser = parsers.get(parsers.size() - 1);
                    parsers.remove(parsers.size() - 1);
                    parsers.add(new KleeneParser(parser));
                    break;
                case "|":
                    Parser inner = toJust(parsers, parsers.get(0).getName());
                    parsers = new ArrayList<>();
                    Parser right = getParser(tokens, tokens.peek());
                    if (right instanceof AndParser) {
                        int size = ((AndParser) right).getParsers().size();
                        if (size == 1) {
                            parsers.add(new OrParser(Arrays.asList(inner, ((AndParser) right).getParsers().get(0))));
                            break;
                        }
                    } else if (right instanceof OrParser) {
                        ((OrParser) right).addParserToFront(inner);
                        parsers.add(right);
                        break;
                    }
                    parsers.add(new OrParser(Arrays.asList(inner, right)));
                    break;
                case "]":
                    break loop;
                case ")":
                    break loop;
                default:
                    parsers.add(new RuleParser(remove));
            }
        }
        return toJust(parsers, name);
    }

    private boolean isTerminating(Queue<String> tokens) {
        if(tokens.isEmpty())
            return true;
        String next = tokens.element();
        return next.equals(")") || next.equals("|") || next.equals("]");
    }

    public Parser getParser() {
        Regex strRegex = new Regex("\'.*?\'$");
        Regex regexRegex = new Regex("\".*?\"$");
        boolean isRegex = regexRegex.match(pattern) != -1;
        if (strRegex.match(pattern) != -1 || isRegex)
            return new TerminalParser(_name, pattern.substring(1, pattern.length() - 1), isRegex);
        List<String> split = Arrays.asList(pattern.split("\\s+"));
        for (String s : Arrays.asList("!", "[", "]", "|", "(", ")", "*")) {
            split = tokenize(split, s);
        }
        return getParser(new ArrayDeque<>(split), _name);
    }

}
