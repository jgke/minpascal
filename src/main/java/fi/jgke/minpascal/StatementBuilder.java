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

public class StatementBuilder {
    StringBuilder builder;

    private boolean hasArgument = false;
    private boolean noIndent = false;
    private int indentation = 0;

    public StatementBuilder() {
        this.builder = new StringBuilder();
    }

    public StatementBuilder append(String str) {
        if (!noIndent) {
            for (int i = 0; i < indentation; i++) {
                builder.append("    ");
            }
        }
        builder.append(str);
        return this;
    }

    public StatementBuilder macroImport(String library) {
        this.append("#import <").append(library).append(">\n");
        return this;
    }

    public StatementBuilder startFunction(String type, String name) {
        this.noIndent = true;
        this.append("\n").append(type).append(" ").append(name).append("(");
        return this;
    }

    public StatementBuilder addArgument(String type, String name) {
        if (hasArgument) {
            this.append(", ");
        }
        this.append(type).append(" ").append(name);
        hasArgument = true;
        return this;
    }

    public StatementBuilder startFunctionBody() {
        this.append(") {\n");
        this.noIndent = false;
        indentation++;
        hasArgument = false;
        return this;
    }

    public StatementBuilder callFunction(String name, String... args) {
        this.append(name);
        this.noIndent = true;
        this.append("(");
        for (String arg : args) {
            if (hasArgument) {
                this.append(", ");
            }
            hasArgument = true;
            this.append(arg);
        }
        this.append(");\n");
        this.noIndent = false;
        hasArgument = false;
        return this;
    }

    public StatementBuilder endFunctionBody() {
        indentation--;
        this.append("}\n");
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
