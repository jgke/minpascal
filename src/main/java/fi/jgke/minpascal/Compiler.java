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

import fi.jgke.minpascal.data.TreeNode;

import java.io.IOException;

public class Compiler {
    public String compile(TreeNode root) throws IOException {
        StatementBuilder output = new StatementBuilder();
        return output
                .macroImport("stdio.h")
                .startFunction("int", "main")
                .addArgument("int", "argc")
                .addArgument("char*", "argv[]")
                .startFunctionBody()
                .callFunction("printf", "\"Hello world!\"")
                .endFunctionBody()
                .toString();
    }
}
