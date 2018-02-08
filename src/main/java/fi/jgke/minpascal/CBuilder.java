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

import com.google.common.collect.Streams;
import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.compiler.IdentifierContext;
import fi.jgke.minpascal.compiler.std.WriteLn;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.nodes.*;
import fi.jgke.minpascal.util.Formatter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CBuilder {
    private StringBuilder builder;

    private int indentation = 0;

    public CBuilder() {
        this.builder = new StringBuilder();
    }

    public CBuilder append(String str) {
        return this.append(str, true);
    }

    public CBuilder append(String str, boolean indent) {
        if (indent) {
            builder.append("\n");
            for (int i = 0; i < indentation; i++) {
                builder.append("    ");
            }
        }
        builder.append(str);
        return this;
    }

    public CBuilder macroImport(String library) {
        this.append("#include <").append(library, false).append(">", false);
        return this;
    }

    public CBuilder addDeclaration(String identifier, CType type) {
        this.append(type.toDeclaration(identifier)).append(";", false);
        return this;
    }

    public CBuilder startFunction(String name, List<String> argumentNames, CType type) {
        IdentifierContext.push();
        this.append("");
        this.append(type.toFunctionDeclaration(argumentNames, name));
        Streams.zip(argumentNames.stream(), type.getSibling().stream(),
                (identifier, subType) -> {
                    IdentifierContext.addIdentifier(identifier, subType);
                    return null;
                });
        this.append(" {", false);
        indentation++;
        return this;
    }

    public CBuilder endFunctionBody() {
        indentation--;
        this.append("}");
        IdentifierContext.pop();
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public void addFunction(String identifier, FunctionNode functionNode) {
        this.startFunction(identifier, functionNode.getParams().getDeclarations().stream()
                        .flatMap(varDeclarationNode -> varDeclarationNode.getIdentifiers().stream()
                                .map(token -> token.getValue().get().toString()))
                        .collect(Collectors.toList()),
                CType.fromFunction(functionNode)
        );
        functionNode.getBody().getChildren().forEach(this::addStatement);
        this.endFunctionBody();
    }

    private Void addBlock(BlockNode body) {
        this.append("{\n");
        body.getChildren().forEach(this::addStatement);
        this.append("}");
        return null;
    }

    private void addStatement(StatementNode statementNode) {
        statementNode.getDeclarationNode().ifPresent(this::addDeclaration);
        statementNode.getSimple().ifPresent(this::addSimple);
        statementNode.getStructured().ifPresent(this::addStructured);
    }

    private void addStructured(StructuredStatementNode structuredStatementNode) {
        structuredStatementNode.map(
                this::addBlock,
                this::addIf,
                this::addWhile
        );
    }

    private Void addWhile(WhileNode whileNode) {
        this.append("while(0) {\n");
        this.indentation++;
        addStatement(whileNode.getStatement());
        this.indentation--;
        this.append("}\n");
        return null;
    }

    private Void addIf(IfThenNode ifThenNode) {
        this.append("if(0) {");
        this.indentation++;
        addStatement(ifThenNode.getThenStatement());
        this.indentation--;
        return null;
    }

    private Void notImplemented(Object any) {
        throw new CompilerException("Not implemented");
    }

    private void addSimple(SimpleStatementNode simpleStatementNode) {
        Formatter.formatTree(simpleStatementNode.toString());
        simpleStatementNode.map(
                this::notImplemented,
                this::notImplemented,
                this::addWrite,
                this::notImplemented,
                this::notImplemented,
                this::notImplemented
        );
    }

    private Void addWrite(WriteNode writeNode) {
        Arrays.stream(WriteLn.fromArguments(writeNode.getArguments()).split("\n"))
                .forEach(this::append);
        return null;
    }

    private void addDeclaration(DeclarationNode declarationNode) {
        declarationNode.getVarDeclaration().ifPresent(var ->
                var.getIdentifiers().forEach(id ->
                        this.addDeclaration(id.getValue().get().toString(), new CType(var.getType()))
                ));
        declarationNode.getFunctionNode().ifPresent($ -> {
            throw new CompilerException("Nested functions not implemented");
        });
    }
}
