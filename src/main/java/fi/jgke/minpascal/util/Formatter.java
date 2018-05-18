package fi.jgke.minpascal.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Formatter {
    public static String formatTree(String tree, int limit) {
        StringBuilder result = new StringBuilder();
        String s = tree
                .replaceAll("(?<!~) ", "")
                .replaceAll("(?<!~)\\(", " (\n")
                .replaceAll("(?<!~)\\[", "[\n")
                .replaceAll("(?<!~),", " \n")
                .replaceAll("(?<!~);", ";\n")
                .replaceAll("(?<!~)\\)", "\n)")
                .replaceAll("(?<!~)]", "\n]");
        int indent = 0;
        for (String s1 : Arrays.stream(s.split("\n")).filter(ss -> !ss.isEmpty()).collect(Collectors.toList())) {
            if (s1.startsWith(")") || s1.startsWith("]"))
                indent--;
            if(limit < 0 || indent < limit) {
                if(!(s1.startsWith(")") || s1.startsWith("]"))) {
                    result.append("\n");
                    for (int i = 0; i < indent; i++) {
                        result.append("  ");
                    }
                }
                result.append(s1.trim());
            }
            if (s1.endsWith("(") || s1.endsWith("["))
                indent++;
        }
        result.append("\n");
        return result.toString();
    }
}
