package fi.jgke.minpascal.system;

import fi.jgke.minpascal.MinPascal;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;

public class SystemTest {
    @Test
    public void example() throws IOException {
        String content = "program example;" +
                "begin\n" +
                "var b, c, d, e : integer;" +
                "var a : integer;" +
                "while a <> b do  WriteLn(\"Waiting\");\n" +
                "\n" +
                "if a > b then WriteLn(\"Condition met\")   {* no semicolon allowed! *}\n" +
                "           else WriteLn(\"Condition not met\");\n" +
                "\n" +
                "" +
                "end\n" +
                ".";

        MinPascal.compile(content);
    }

    @Test
    public void helloWorldMpp() {
        int code = MinPascal.app(new String[]{"helloWorld.mpp"}, new PrintWriter(System.out), new PrintWriter(System.err));
        assertThat(code, is(equalTo(0)));
    }


    @Test
    public void helloWorldInFile() throws IOException, InterruptedException {
        String content = "program example;" +
                "begin\n" +
                "  WriteLn(\"Condition met\")\n" +
                "end\n" +
                ".";

        withMppFile(content, path -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter outw = new PrintWriter(new PrintStream(out));
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            PrintWriter errw = new PrintWriter(new PrintStream(err));
            String[] args = {path.toAbsolutePath().toString()};
            MinPascal.app(args, outw, errw);
            String output = err.toString();
            assertThat(output, isEmptyString());
        });
    }

    @FunctionalInterface
    interface IOConsumer<T> {
        void accept(T param) throws IOException, InterruptedException;
    }


    static void withMppFile(String content, IOConsumer<Path> consumer) throws IOException, InterruptedException {
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
            target = Paths.get(p.toString().replaceAll(".mpp$", ""));
            if (Files.exists(target)) {
                Files.delete(target);
            }
        }
        Thread.sleep(500);
    }
}
