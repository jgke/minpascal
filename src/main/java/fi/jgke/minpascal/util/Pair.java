package fi.jgke.minpascal.util;

import lombok.Data;

@Data
public class Pair<L, R> {
    private final L left;
    private final R Right;
}
