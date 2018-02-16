package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.compiler.IdentifierContext;
import fi.jgke.minpascal.parser.nodes.ArgumentsNode;
import fi.jgke.minpascal.parser.nodes.ExpressionNode;
import fi.jgke.minpascal.parser.nodes.VariableNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StdIO {
    public static String writeLn(ArgumentsNode node) {
        StringBuilder fmt = new StringBuilder();
        List<String> args = new ArrayList<>();
        List<List<String>> steps = new ArrayList<>();
        List<String> post = new ArrayList<>();
        for (ExpressionNode arg : node.getArguments()) {
            CExpressionResult result = CExpressionResult.fromExpression(arg);
            fmt.append(result.getType().toFormat()).append(" ");
            args.add(result.getIdentifier());
            steps.add(result.getTemporaries());
            post.addAll(result.getPost());
        }

        String pre = steps.stream()
                .map(list -> list.stream()
                        .collect(Collectors.joining("\n")) + "\n")
                .collect(Collectors.joining("\n")) + "\n";
        String print = "printf(\"" + fmt.toString().trim() + "\\n\", " + args.stream().collect(Collectors.joining(", ")) + ");\n";
        String clean = post.stream().collect(Collectors.joining("\n")) + "\n";
        return pre + print + clean;
    }

    // Segvs on long strings, or usually strings in general
    public static String read(List<VariableNode> variables) {
       return variables.stream()
               .map(StdIO::readVariable)
               .collect(Collectors.joining(""));
    }

    private static String readVariable(VariableNode variableNode) {
        CType type = IdentifierContext.getType(variableNode.getIdentifier().getValue());
        String fmtFormat = "scanf(\"" + type.toFormat() + "\", &";
        if (type.equals(CType.CSTRING)) {
            fmtFormat = "_builtin_scanstring(&";
        }
        return fmtFormat + variableNode.getIdentifier().getValue() + ");\n";
    }
}
