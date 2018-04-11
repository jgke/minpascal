package fi.jgke.minpascal.parser.astparser;

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
}
