package fi.jgke.minpascal.system;

import fi.jgke.minpascal.MinPascal;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;

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

    @Test
    public void helloWorldInFile() throws IOException {
        String content = "program example;" +
                "begin\n" +
                "  WriteLn('Condition met')\n" +
                "end\n" +
                ".";

        withMppFile(content, path -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter outw = new PrintWriter(new PrintStream(out));
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            PrintWriter errw = new PrintWriter(new PrintStream(err));
            String[] args = {path.toAbsolutePath().toString()};
            System.out.println(path.toAbsolutePath().toString());
            MinPascal.app(args, outw, errw);
            String output = err.toString();
            assertThat(output, isEmptyString());
        });
    }

    private void withMppFile(String content, Consumer<Path> consumer) throws IOException {
        Path p = Files.createTempFile(null, ".mpp");
        try (OutputStream s = Files.newOutputStream(p.toAbsolutePath())) {
            s.write(content.getBytes());
            s.close();
            consumer.accept(p);
        } finally {
            Files.delete(p);
            Path target = Paths.get(p.toString().replaceAll(".mpp$", ".c"));
            if (Files.exists(target)) {
                Files.delete(target);
            }
        }
    }
}
