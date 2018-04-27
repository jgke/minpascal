package fi.jgke.minpascal.util;

import lombok.Data;

@Data
public class Tuple3<T1, T2, T3> {
    private final T1 first;
    private final T2 second;
    private final T3 third;
}
