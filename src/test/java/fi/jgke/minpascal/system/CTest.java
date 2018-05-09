package fi.jgke.minpascal.system;

import fi.jgke.minpascal.MinPascal;
import lombok.Getter;
import org.junit.Test;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static fi.jgke.minpascal.system.SystemTest.withMppFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class CTest {

    @Test
    public void helloWorld() {
        String app = "program Hello;\n" +
                "begin\n" +
                "  writeln (\"Hello world!\");\n" +
                "end.\n";
        testCompiledOutput(app, "Hello world!\n");
    }

    @Test
    public void ifClause() {
        String app = "program Hello;\n" +
                "begin\n" +
                "  if false then writeln (\"this is not printed\");\n" +
                "  if true then writeln (\"this is printed\");\n" +
                "end.\n";
        testCompiledOutput(app, "this is printed\n");
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        @Getter
        private String stream = "";

        public StreamGobbler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            stream = new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .collect(Collectors.joining("\n")) + '\n';
        }
    }

    private static int exec(String command, File path, Consumer<String> stdout, Consumer<String> stderr) throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(path);
        builder.command("sh", "-c", command);
        Process process = builder.start();
        StreamGobbler stdoutGobbler = new StreamGobbler(process.getInputStream());
        StreamGobbler stderrGobbler = new StreamGobbler(process.getErrorStream());
        Executors.newSingleThreadExecutor().submit(stdoutGobbler);
        Executors.newSingleThreadExecutor().submit(stderrGobbler);
        int returnCode = process.waitFor();
        Thread.sleep(100); // give up cpu for output streams... without this, the
                               // function might return before stdout/stderr has content
        stdout.accept(stdoutGobbler.getStream());
        stderr.accept(stderrGobbler.getStream());
        return returnCode;
    }

    public static void testCompiledOutput(String input, String output) {
        testCompiledOutput(input, output, 0);
    }

    public static void testCompiledOutput(String input, String output, int expectedReturnCode) {
        try {
            withMppFile(input, path -> {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PrintWriter outw = new PrintWriter(new PrintStream(out));
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                PrintWriter errw = new PrintWriter(new PrintStream(err));

                String base = path.toAbsolutePath().toString().replaceAll(".mpp$", "");

                String[] args = {base + ".mpp", base + ".c"};
                int returncode = MinPascal.app(args, outw, errw);

                outw.flush();
                errw.flush();
                assertThat("MinPascal stdout is empty", out.toString(), is(equalTo("")));
                assertThat("MinPascal stderr is empty", err.toString(), is(equalTo("")));
                assertThat(returncode, is(equalTo(0)));

                returncode = exec("gcc " + base + ".c -Werror -o " + base,
                        path.getParent().toFile(), CTest::stripPrint, CTest::stripPrint);
                assertThat(returncode, is(equalTo(0)));

                AtomicReference<String> mutOutput = new AtomicReference<>("");

                AtomicReference<String> valgrindOutput = new AtomicReference<>("");

                // this fails on windows but w/e
                boolean hasValgrind = exec("which valgrind",
                        path.getParent().toFile(), mutOutput::set, valgrindOutput::set) == 0;

                String valgrind = hasValgrind ? "valgrind --error-exitcode=1 " : "";

                returncode = exec(valgrind + base,
                        path.getParent().toFile(), mutOutput::set, valgrindOutput::set);
                if (returncode != 0) {
                    System.err.println(valgrind + base);
                    System.err.println(path.getParent().toFile());
                    stripPrint(valgrindOutput.get());
                    stripPrint(mutOutput.get());
                }
                assertThat(returncode, is(equalTo(expectedReturnCode)));
                assertThat(mutOutput.get(), is(equalTo(output)));
            });
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void stripPrint(String param) {
        System.err.print(param.trim());
        if (!param.trim().isEmpty()) {
            System.err.println();
        }
    }
}
