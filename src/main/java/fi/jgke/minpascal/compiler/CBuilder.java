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

import com.google.common.collect.Streams;
import fi.jgke.minpascal.compiler.std.CExpressionResult;
import fi.jgke.minpascal.compiler.std.StdIO;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.exception.CompilerException;
import fi.jgke.minpascal.parser.nodes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CBuilder {
    private StringBuilder builder;
    private List<CBuilder> functions;
    private List<String> imports;

    private int indentation = 0;

    public CBuilder() {
        this.builder = new StringBuilder();
        this.functions = new ArrayList<>();
        this.imports = new ArrayList<>();
    }

    public CBuilder append(String str) {
        return this.append(str, true);
    }

    private CBuilder append(String str, boolean indent) {
        if (indent) {
            builder.append("\n");
            for (int i = 0; i < indentation; i++) {
                builder.append("    ");
            }
        }
        builder.append(str);
        return this;
    }

    public CBuilder standardLibraryFunction(String fn) {
        this.imports.add(fn);
        return this;
    }

    public CBuilder macroImport(String library) {
        this.imports.add("#include <" + library + ">\n");
        return this;
    }

    private CBuilder addDeclaration(String identifier, CType type) {
        this.append(type.toDeclaration(identifier)).append(" = " + type.defaultValue() + ";", false);
        IdentifierContext.addIdentifier(identifier, type);
        return this;
    }

    private CBuilder startFunction(String name, List<String> argumentNames, CType type) {
        IdentifierContext.addIdentifier(name, type);
        IdentifierContext.push();
        this.append("");
        this.append(type.toFunctionDeclaration(argumentNames, name));
        Streams.forEachPair(argumentNames.stream(), type.getSibling().stream(), IdentifierContext::addIdentifier);
        this.append(" {", false);
        indentation++;
        return this;
    }

    private CBuilder endFunctionBody() {
        indentation--;
        this.append("}");
        IdentifierContext.pop();
        return this;
    }

    private CBuilder addLabel(String label) {
        this.append("\n" + label + ":;", false); // left align labels
        return this;
    }

    private CBuilder addGoto(String label) {
        this.append("goto " + label + ";");
        return this;
    }

    public Stream<CBuilder> getFunctions() {
        return Stream.concat(
                functions.stream().flatMap(CBuilder::getFunctions),
                Stream.of(this));
    }

    @Override
    public String toString() {
        return this.toString(getFunctions().filter(cb -> !cb.equals(this)));
    }

    public String toString(Stream<CBuilder> builders) {
        return imports.stream().collect(Collectors.joining("")) +
                builders
                        .map(cBuilder -> cBuilder.toString(Stream.empty()))
                        .collect(Collectors.joining("\n")) +
                "\n" + this.builder.toString();
    }

    public void addFunction(String identifier, FunctionNode functionNode) {
        CBuilder cBuilder = new CBuilder()
                .startFunction(identifier, functionNode.getParams().getDeclarations().stream()
                                .flatMap(varDeclarationNode -> varDeclarationNode.getIdentifiers().stream()
                                        .map(Token::getValue))
                                .collect(Collectors.toList()),
                        CType.fromFunction(functionNode));
        functionNode.getBody().getChildren().forEach(cBuilder::addStatement);
        cBuilder.endFunctionBody();
        this.functions.add(cBuilder);
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
        String again = IdentifierContext.genIdentifier("again");
        String end = IdentifierContext.genIdentifier("end");

        this.addLabel(again);

        CExpressionResult result = CExpressionResult.fromExpression(whileNode.getCondition());
        result.getTemporaries().forEach(this::append);
        this.append("if(!" + result.getIdentifier() + ") ");
        this.indentation++;
        addGoto(end);
        this.indentation--;

        addStatement(whileNode.getStatement());
        this.addGoto(again);
        this.addLabel(end);
        return null;
    }

    private Void addIf(IfThenNode ifThenNode) {
        String end = IdentifierContext.genIdentifier("iffalse");

        CExpressionResult result = CExpressionResult.fromExpression(ifThenNode.getCondition());
        result.getTemporaries().forEach(this::append);

        this.append("if(!" + result.getIdentifier() + ") ");
        indentation++;
        addGoto(end);
        indentation--;

        addStatement(ifThenNode.getThenStatement());

        if (ifThenNode.getElseStatement().isPresent()) {
            StatementNode elseStatement = ifThenNode.getElseStatement().get();
            String label2 = IdentifierContext.genIdentifier("end");

            this.addGoto(label2);

            this.addLabel(end);
            addStatement(elseStatement);

            end = label2;
        }
        this.addLabel(end);
        return null;
    }

    private Void notImplemented(Object any) {
        throw new CompilerException("Not implemented");
    }

    private void addSimple(SimpleStatementNode simpleStatementNode) {
        simpleStatementNode.map(
                this::addReturn,
                this::addRead,
                this::addWrite,
                this::addAssert,
                this::addCall,
                this::addAssign
        );
    }

    private Void addAssign(AssignmentNode assignmentNode) {
        CExpressionResult cExpressionResult = CExpressionResult.fromExpression(assignmentNode.getValue());
        cExpressionResult.getTemporaries().forEach(this::append);
        String identifier = assignmentNode.getIdentifier().getIdentifier().getValue();
        if (assignmentNode.getIdentifier().getArrayAccessInteger().isPresent()) {
            ExpressionNode e = assignmentNode.getIdentifier().getArrayAccessInteger().get();
            CExpressionResult accessInt = CExpressionResult.fromExpression(e);
            accessInt.getTemporaries().forEach(this::append);
            identifier += "[" + accessInt.getIdentifier() + "]";
            accessInt.getPost().forEach(this::append);
        }
        this.append(identifier + " = " + cExpressionResult.getIdentifier() + ";");
        cExpressionResult.getPost().forEach(this::append);
        return null;
    }

    private Void addCall(CallNode callNode) {
        List<CExpressionResult> collect = callNode.getArguments().getArguments().stream()
                .map(CExpressionResult::fromExpression)
                .collect(Collectors.toList());
        collect.forEach(e -> e.getTemporaries().forEach(this::append));
        this.append(callNode.getIdentifier().getValue() + "(");
        this.append(collect.stream()
                .map(CExpressionResult::getIdentifier)
                .collect(Collectors.joining(", ")), false);
        this.append(");", false);
        collect.forEach(e -> e.getPost().forEach(this::append));
        return null;
    }

    private Void addAssert(AssertNode assertNode) {
        CExpressionResult cExpressionResult = CExpressionResult.fromExpression(assertNode.getBooleanExpr());
        cExpressionResult.getTemporaries().forEach(this::append);
        this.append("assert " + cExpressionResult.getIdentifier() + ";");
        cExpressionResult.getPost().forEach(this::append);
        return null;
    }

    private Void addReturn(ReturnNode returnNode) {
        ExpressionNode expression = returnNode.getExpression();
        CExpressionResult cExpressionResult = CExpressionResult.fromExpression(expression);
        cExpressionResult.getTemporaries().forEach(this::append);
        cExpressionResult.getPost().forEach(this::append);
        this.append("return " + cExpressionResult.getIdentifier() + ";"); // leak memory for now
        return null;
    }

    private Void addRead(ReadNode readNode) {
        Arrays.stream(StdIO.read(readNode.getVariables()).split("\n"))
                .forEach(this::append);
        return null;
    }

    private Void addWrite(WriteNode writeNode) {
        Arrays.stream(StdIO.writeLn(writeNode.getArguments()).split("\n"))
                .forEach(this::append);
        return null;
    }

    private void addDeclaration(DeclarationNode declarationNode) {
        declarationNode.getVarDeclaration().ifPresent(var ->
                var.getIdentifiers().forEach(id ->
                        this.addDeclaration(id.getValue(), new CType(var.getType()))
                ));
        declarationNode.getFunctionNode().ifPresent(functionNode -> {
            this.addFunction(functionNode.getIdentifier().getValue(), functionNode);
        });
    }
}
