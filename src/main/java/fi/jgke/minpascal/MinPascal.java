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

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.Parser;
import fi.jgke.minpascal.tokenizer.Tokenizer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MinPascal {

    public static int app(String[] args, PrintStream out, PrintStream err) throws IOException {
        if (args.length < 1 || args.length > 2) {
            err.println("Invalid number of arguments: expected one or two");
            return -1;
        }

        Path source = Paths.get(args[0]);
        if (!Files.exists(source)) {
            err.println("Invalid argument: file not found");
            return -1;
        }

        if(args.length == 1 && !source.toString().endsWith(".mpp")) {
            err.println("Invalid source file parameter: You need to provide a " +
                    "target file if you don't have the correct extension (.mpp)");
            return -1;
        }

        Path target;
        if(args.length == 2) {
            target = Paths.get(args[1]);
        } else {
            target = Paths.get(source.toString().replaceAll(".mpp$", ".c"));
        }

        try {
            Tokenizer tokenizer = new Tokenizer();
            Parser parser = new Parser();
            Compiler compiler = new Compiler();

            String content = new String(Files.readAllBytes(Paths.get(args[0])));

            ArrayDeque<Character> characterStream = new ArrayDeque<>();
            for(char c : content.toCharArray())
                characterStream.add(c);

            Stream<Token> tokenize = tokenizer.tokenize(characterStream);
            parser.parse(tokenize);
            compiler.compile(content, target);
        } catch (RuntimeException e) {
            err.println(e.getMessage());
            return 1;
        }
        return 0;
    }

    public static void main(String[] args) throws IOException {
        System.exit(app(args, System.out, System.err));
    }

}
