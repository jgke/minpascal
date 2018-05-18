package fi.jgke.minpascal.data;

import lombok.Data;

@Data
public class Position {
    private final int line;
    private final int column;

    @Override
    public String toString() {
        return "line " + getLine() + ", column " + getColumn();
    }

    public Position addStr(String s) {
        if (s.length() > 30) {
            throw new RuntimeException();
        }
        if (s.contains("\n")) {
            String[] split = s.split("\n", -1);
            int length = split[split.length - 1].length() + 1;
            int lines = split.length - 1;
            // ignore column here because we want it from the start of the line
            return new Position(line + lines, length);
        }
        return new Position(line, column + s.length());
    }
}
