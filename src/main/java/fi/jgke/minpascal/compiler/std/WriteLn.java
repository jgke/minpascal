package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.parser.nodes.ArgumentsNode;
import fi.jgke.minpascal.parser.nodes.ExpressionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WriteLn {
    public static String fromArguments(ArgumentsNode node) {
        StringBuilder fmt = new StringBuilder();
        List<String> args = new ArrayList<>();
        List<String> steps = new ArrayList<>();
        List<String> post = new ArrayList<>();
        for (ExpressionNode arg : node.getArguments()) {
            CExpressionResult result = CExpressionResult.fromExpression(arg);
            fmt.append(result.getType().toFormat()).append(" ");
            args.add(result.getIdentifier());
            steps.add(result.getTemporaries());
            post.add(result.getPost());
        }
        return steps.stream().map(str -> str.replaceAll(";", ";\n")).collect(Collectors.joining()) +
                "\nprintf(\"" + fmt.toString().trim() + "\\n\", " +
                args.stream().collect(Collectors.joining(", ")) + ");\n\n" +
                post.stream().map(str -> str.replaceAll(";", ";\n")).collect(Collectors.joining());
    }
}
