package fi.jgke.minpascal.system;

import fi.jgke.minpascal.MinPascal;
import org.junit.Test;

import java.io.IOException;

public class SystemTest {
    @Test
    public void example() throws IOException {
        String content = "program example;" +
                "begin\n" +
                "while a <> b do  WriteLn('Waiting');\n" +
                "\n" +
                "if a > b then WriteLn('Condition met')   {* no semicolon allowed! *}\n" +
                "           else WriteLn('Condition not met');\n" +
                "\n" +
                "" +
                "end\n" +
                ".";

        MinPascal.compile(content);
    }
}
