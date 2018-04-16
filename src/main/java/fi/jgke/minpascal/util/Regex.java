package fi.jgke.minpascal.util;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

@Data
// Limited, non-backtracking regex-like
public class Regex {
    private final Pattern pattern;

    public Regex(String pattern) {
        Queue<Character> queue = new ArrayDeque<>();
        for (char c : pattern.toCharArray()) {
            queue.add(c);
        }

        this.pattern = makePattern(queue);
        if (!queue.isEmpty()) throw new AssertionError();
    }

    private enum Type {
        STRING,
        ANY,
        KLEENE,
        KLEENE_LAZY,
        KLEENE_PLUS,
        CONCAT,
        GROUP,
        NEG_GROUP,
        END
    }

    @Getter
    @ToString
    private class Pattern {
        private final Type type;
        private final List<Pattern> content;
        private final Pattern lazyMatch;
        private final String terminal;

        Pattern(Type type, List<Pattern> content) {
            this.type = type;
            this.content = content;
            this.terminal = null;
            this.lazyMatch = null;
        }

        Pattern(String terminal) {
            this.type = Type.STRING;
            this.content = null;
            this.terminal = terminal;
            this.lazyMatch = null;
        }

        Pattern(Type type, List<Pattern> content, Pattern lazyMatch) {
            this.type = type;
            this.content = content;
            this.lazyMatch = lazyMatch;
            this.terminal = null;
        }

        private int getMatches(String str) {
            int matchSize = 0;
            int position = 0;
            switch (getType()) {
                case STRING:
                    if (str.startsWith(getTerminal())) {
                        return getTerminal().length();
                    }
                    return -1;
                case ANY:
                    if (str.isEmpty())
                        return -1;
                    return 1;
                case KLEENE:
                case KLEENE_PLUS:
                    assert content != null;
                    while (!str.isEmpty()) {
                        for (Pattern subPattern : content) {
                            int subSize = subPattern.getMatches(str.substring(matchSize + position));
                            if (subSize == -1) {
                                position = 0;
                                break;
                            }
                            position += subSize;
                        }
                        if (position == 0)
                            break;
                        matchSize += position;
                        position = 0;
                    }
                    if (getType().equals(Type.KLEENE_PLUS) && matchSize == 0)
                        return -1;
                    return matchSize;
                case KLEENE_LAZY:
                    int lazySize;
                    assert content != null;
                    while ((lazySize = getLazyMatch().getMatches(str.substring(position))) == -1) {
                        for (Pattern subPattern : content) {
                            int subSize = subPattern.getMatches(str.substring(position));
                            if (subSize == -1) {
                                position = 0;
                                break;
                            }
                            position += subSize;
                        }
                        if (position == 0)
                            break;
                        matchSize += position;
                    }
                    return position + lazySize;
                case CONCAT:
                    assert content != null;
                    for (Pattern subPattern : content) {
                        int subSize = subPattern.getMatches(str.substring(position));
                        if (subSize == -1) {
                            return -1;
                        }
                        position += subSize;
                    }
                    return position;
                case GROUP:
                    assert content != null;
                    for (Pattern subPattern : content) {
                        int subSize = subPattern.getMatches(str.substring(position));
                        if (subSize != -1) {
                            return subSize;
                        }
                    }
                    return -1;
                case NEG_GROUP:
                    assert content != null;
                    for (Pattern subPattern : content) {
                        int subSize = subPattern.getMatches(str.substring(position));
                        if (subSize != -1) {
                            return -1;
                        }
                    }
                    return 1;
                case END:
                    if (str.isEmpty()) {
                        return 0;
                    }
                    return -1;
            }
            throw new RuntimeException("unreachable code");
        }
    }

    private Pattern makePattern(Queue<Character> queue) {
        List<Pattern> orPatterns = new ArrayList<>();
        List<Pattern> patterns = new ArrayList<>();
        loop:
        while (!queue.isEmpty()) {
            Pattern pattern;
            char c = queue.remove();
            switch (c) {
                case '\\':
                    patterns.add(new Pattern("" + queue.remove()));
                    break;
                case '.':
                    patterns.add(new Pattern(Type.ANY, Collections.emptyList()));
                    break;
                case '$':
                    patterns.add(new Pattern(Type.END, Collections.emptyList()));
                    break;
                case '*':
                case '+':
                    Pattern lastPattern = patterns.get(patterns.size() - 1);
                    ArrayList<Pattern> rest = new ArrayList<>(patterns.subList(0, patterns.size() - 1));
                    if (!queue.isEmpty() && queue.peek() == '?') {
                        queue.remove();
                        Pattern subPattern;
                        subPattern = makePattern(queue);
                        pattern = new Pattern(Type.KLEENE_LAZY,
                                Collections.singletonList(lastPattern),
                                subPattern);
                    } else {
                        Type type = c == '+' ? Type.KLEENE_PLUS : Type.KLEENE;
                        pattern = new Pattern(type, Collections.singletonList(lastPattern));
                    }
                    patterns = rest;
                    patterns.add(pattern);
                    break;
                case '|':
                    pattern = patterns.size() == 1
                            ? patterns.get(0)
                            : new Pattern(Type.CONCAT, patterns);
                    orPatterns.add(pattern);
                    patterns = new ArrayList<>();
                    break;
                case '(':
                    patterns.add(makePattern(queue));
                    break;
                case ')':
                    break loop;
                case '[':
                    List<Pattern> group = new ArrayList<>();
                    Type type = Type.GROUP;
                    if (queue.peek() == '^') {
                        queue.remove();
                        type = Type.NEG_GROUP;
                    }
                    while (true) {
                        Character rangeStart = queue.remove();
                        if (!queue.isEmpty() && queue.peek() == '-') {
                            queue.remove();
                            if (queue.peek() == ']') { // '-' is a character we want to match
                                queue.remove();
                                group.add(new Pattern("" + rangeStart));
                                group.add(new Pattern("-"));
                                break;
                            }
                            Character rangeEnd = queue.remove();
                            for (char i = rangeStart; i < rangeEnd; i++) {
                                group.add(new Pattern("" + i));
                            }
                        }
                        group.add(new Pattern("" + rangeStart));
                        if (queue.peek() == ']') {
                            queue.remove();
                            break;
                        }
                    }
                    patterns.add(new Pattern(type, group));
                    break;
                default:
                    patterns.add(new Pattern("" + c));
            }
        }
        if (orPatterns.isEmpty()) {
            return new Pattern(Type.CONCAT, patterns);
        }
        if (!patterns.isEmpty()) {
            orPatterns.add(new Pattern(Type.CONCAT, patterns));
        }
        return new Pattern(Type.GROUP, orPatterns);
    }

    public int match(String match) {
        return pattern.getMatches(match);
    }
}
