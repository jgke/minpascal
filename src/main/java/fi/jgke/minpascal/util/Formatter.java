package fi.jgke.minpascal.util;

public class Formatter {
    public static String formatTree(String tree) {
        StringBuilder result = new StringBuilder();
        String s = tree
                .replaceAll(" ", "")
                .replaceAll("\\(", "(\n")
                .replaceAll("\\[", "[\n")
                .replaceAll(",", ",\n")
                .replaceAll(";", ";\n")
                .replaceAll("\\)", "\n)")
                .replaceAll("]", "\n]");
        int indent = 0;
        for (String s1 : s.split("\n")) {
            if (s1.startsWith(")") || s1.startsWith("]"))
                indent--;
            for (int i = 0; i < indent; i++) {
                result.append("  ");
            }
            result.append(s1);
            result.append("\n");
            if (s1.endsWith("(") || s1.endsWith("["))
                indent++;
        }
        return result.toString();
    }
}
