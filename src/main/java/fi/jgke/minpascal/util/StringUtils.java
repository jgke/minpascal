package fi.jgke.minpascal.util;

public class StringUtils {
    public static String ellipsis(String s, int maxlen) {
        if (s.length() < maxlen - 3)
            return s;
        return s.substring(0, maxlen - 3) + "...";
    }
}
