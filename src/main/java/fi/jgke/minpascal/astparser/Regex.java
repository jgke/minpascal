package fi.jgke.minpascal.astparser;

import lombok.Data;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class Regex {
    private final String pattern;

    public boolean matches(String str) {
        return Pattern.compile(pattern).matcher(str).lookingAt();
    }

    public List<String> getGroups(String str) {
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        matcher.find();
        return IntStream
                .range(0, matcher.groupCount() + 1) // indexing starts at 1, 0 is full match
                .mapToObj(matcher::group)
                .collect(Collectors.toList());
    }

    public static String quote(String match) {
        return Pattern.quote(match);
    }
/*
    private enum Type {
        STRING,
        KLEENE,
        KLEENE_LAZY,
        OP, CP,
        OB, CB,
        OR
    }

    private void addTwo(List<Pair<Type, String>> list, String pre, Type type) {
        if (!pre.isEmpty())
            list.add(new Pair<>(Type.STRING, pre));
        list.add(new Pair<>(type, ""));
    }

    private void groups(String match) {
        Queue<Character> queue = new ArrayDeque<>();
        for (char c : match.toCharArray()) {
            queue.add(c);
        }
        List<Pair<Type, String>> tokens = new ArrayList<>();
        tokens.add(new Pair<>(Type.OP, ""));
        String match = "";
        outer:
        while (!queue.isEmpty()) {
            char c = queue.remove();
            String token = "";
            while (!queue.isEmpty()) {
                switch (c) {
                    case '\\':
                        token += queue.remove();
                        continue;
                    case '*':
                        if (!queue.isEmpty() && queue.peek() == '?') {
                            queue.remove();
                            addTwo(tokens, token, Type.KLEENE_LAZY);
                        } else {
                            addTwo(tokens, token, Type.KLEENE);
                        }
                        continue outer;
                    case '|':
                        addTwo(tokens, token, Type.OR);
                        continue outer;
                    case '(':
                        addTwo(tokens, token, Type.OP);
                        continue outer;
                    case ')':
                        addTwo(tokens, token, Type.CP);
                        continue outer;
                    case '[':
                        addTwo(tokens, token, Type.OB);
                        continue outer;
                    case ']':
                        addTwo(tokens, token, Type.CB);
                        continue outer;
                }
            }
            tokens.add(new Pair<>(Type.STRING, token));
        }
        tokens.add(new Pair<>(Type.CP, ""));
    }

    private Optional<Map<Integer, String>> getGroups(List<Pair<Type, String>> tokens, String str, int group, int pos) {
        Pair<Type, String> token = tokens.get(pos);
        String match = "";
        for (int i = pos; i < tokens.size(); i++) {
            switch (token.getLeft()) {
                case STRING:
                    if (str.startsWith(token.getRight())) {
                        match += token.getRight();
                        break;
                    }
                    return Optional.empty();
                case KLEENE:
                    break;
                case KLEENE_LAZY:
                    break;
                case OP:
                    break;
                case CP:
                    break;
                case OB:
                    break;
                case CB:
                    break;
                case OR:
                    break;
            }
        }
    }
    */
}
