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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CBuilder {
    private StringBuilder builder;
    private List<String> imports;

    public CBuilder() {
        this.builder = new StringBuilder();
        this.imports = new ArrayList<>();
    }

    public CBuilder append(String str) {
        builder.append("\n");
        builder.append(str);
        return this;
    }

    public CBuilder macroImport(String library) {
        this.imports.add("#include <" + library + ">\n");
        return this;
    }

    @Override
    public String toString() {
        return imports.stream().collect(Collectors.joining("")) +
                "\n" + this.builder.toString() + "\n";
    }

}
