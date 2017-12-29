package fi.jgke.minpascal;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.base.ParseQueue;

import java.util.Arrays;

public class TestUtils {
    public static ParseQueue queueWith(Token... tokens) {
        ParseQueue queue = new ParseQueue();
        queue.addAll(Arrays.asList(tokens));
        return queue;
    }
}
