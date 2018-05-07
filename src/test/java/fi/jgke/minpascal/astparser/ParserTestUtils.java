package fi.jgke.minpascal.astparser;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParserTestUtils {
    static void initRules(String... rules) {
        String ws = "whitespace ::= \" *\"";
        AstParser.setRules(Stream.concat(Arrays.stream(rules), Stream.of(ws)).map(Rule::new)
                .collect(Collectors.toMap(Rule::get_name, Rule::getParser)));
    }
}
