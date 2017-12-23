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
}
