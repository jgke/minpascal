package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.parser.nodes.ArgumentsNode;

import java.util.List;

public class WriteLn {
    public static String fromArguments(ArgumentsNode node) {
        String fmt;
        List<String> args;
        List<String> steps;
        node.getArguments().forEach(arg -> {
            CExpressionResult result = CExpressionResult.fromExpression(arg);
        });
        return "foo";
    }
}
