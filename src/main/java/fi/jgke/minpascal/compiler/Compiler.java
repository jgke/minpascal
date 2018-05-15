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
package fi.jgke.minpascal.compiler;

import fi.jgke.minpascal.astparser.nodes.AstNode;

import java.util.Arrays;
import java.util.Collections;

public class Compiler {
    private CBuilder output;

    private Compiler(CBuilder output) {
        this.output = output;
    }

    private Compiler block(AstNode root) {
        CRootBlock cRootBlock = new CRootBlock(// @formatter:off
                Arrays.asList("stdio.h", "stdlib.h", "stdbool.h", "string.h"),
                Arrays.asList(
                        "void _builtin_scanstring(char **str) {" +
                                "size_t strSize = 1024;" +
                                "size_t pos = 0;" +
                                "int c = 0;" +
                                "int white = 0;" +
                                "*str = NULL;" +
                                "while(1) {" +
                                    "*str = realloc(*str, strSize);" +
                                    "while(pos < strSize-1) {" +
                                        "c = getchar();" +
                                        "if(c == EOF || !c || (white && c == '\\n')) " +
                                            "goto end;" +
                                        "if(white || (c != '\\n' && c != ' ')) " +
                                            "(*str)[pos++] = (char)c;" +
                                        "if(c != '\\n' && c != ' ') white = 1;" +
                                    "}" +
                                    "strSize *= 1.5;" +
                                "}" +
                        "end:" +
                                "(*str)[pos] = '\\0';" +
                        "}",

                        "char *_builtin_strdup(const char *str) {" +
                                "char *out = malloc(strlen(str)+1);" +
                                "strcpy(out, str);" +
                                "return out;" +
                        "}"),
                // @formatter:on
                Collections.emptyList()
        );

        cRootBlock.getLibraries().forEach(output::macroImport);
        cRootBlock.getStdFunctions().forEach(output::append);

        IdentifierContext.push();
        //root.debug();
        RootBuilder rootBuilder = new RootBuilder(root);
        rootBuilder.build(output);
        IdentifierContext.pop();

        return this;
    }

    public static String compile(AstNode root) {
        return new Compiler(new CBuilder()).block(root).toString();
    }

    @Override
    public String toString() {
        return output.toString();
    }
}
