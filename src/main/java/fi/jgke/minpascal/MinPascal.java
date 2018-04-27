/*
 * Copyright 2017 Jaakko Hannikainen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.jgke.minpascal;

import fi.jgke.minpascal.astparser.AstParser;
import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.Compiler;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MinPascal {

    public static String compile(String content) throws IOException {
        AstNode parse = AstParser.parse(content);
        return Compiler.compile(parse);
    }

    public static int app(String[] args, PrintWriter out, PrintWriter err) {
        ArgumentParser parser = ArgumentParsers.newFor("minpascal").build()
                .defaultHelp(true)
                .description("Compile minipascal files to simplified C.");
        parser.addArgument("-s", "--strict").setDefault(false)
                .help("Strict mode - follow specification to the letter");
        parser.addArgument("file").required(true)
                .help("Input file");
        parser.addArgument("outFile").nargs("?").setDefault((Object) null)
                .help("Output file");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e, err);
            return -1;
        }

        Configuration.STRICT_MODE = ns.getBoolean("strict");
        Path source, target;

        source = Paths.get(ns.getString("file"));

        if (!Files.exists(source)) {
            err.println("Invalid argument: file not found");
            return -1;
        }

        if (ns.get("outFile") != null) {
            target = Paths.get(ns.getString("outFile"));
        } else {
            if (!source.toString().endsWith(".mpp")) {
                err.println("Invalid source file parameter: You need to provide a " +
                                    "target file if you don't have the correct extension (.mpp)");
                return -1;
            }
            target = Paths.get(source.toString().replaceAll(".mpp$", ".c"));
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(args[0])));
            String compiled = compile(content);
            Files.write(target, compiled.getBytes());
        } catch (Throwable e) {
            e.printStackTrace(err);
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    public static void main(String[] args) {
        int exitCode;
        try (PrintWriter out = new PrintWriter(System.out, false)) {
            try (PrintWriter err = new PrintWriter(System.err, false)) {
                exitCode = app(args, out, err);
                err.flush();
            }
            out.flush();
        }
        System.exit(exitCode);
    }

}
