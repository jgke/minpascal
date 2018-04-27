package fi.jgke.minpascal.compiler.std;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.compiler.IdentifierContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StdIO {
    public static String writeLn(AstNode node) {
        StringBuilder fmt = new StringBuilder();
        List<String> args = new ArrayList<>();
        List<List<String>> steps = new ArrayList<>();
        List<String> post = new ArrayList<>();
        /*
        for (AstNode arg : node.getArguments()) {
            CExpressionResult result = CExpressionResult.fromExpression(arg);
            fmt.append(result.getType().toFormat()).append(" ");
            args.add(result.getIdentifier());
            steps.add(result.getTemporaries());
            post.addAll(result.getPost());
        }*/

        String pre = steps.stream()
                .map(list -> list.stream()
                        .collect(Collectors.joining("\n")) + "\n")
                .collect(Collectors.joining("\n")) + "\n";
        String print = "printf(\"" + fmt.toString().trim() + "\\n\", " + args.stream().collect(Collectors.joining(", ")) + ");\n";
        String clean = post.stream().collect(Collectors.joining("\n")) + "\n";
        throw new RuntimeException("not impl");
       // return pre + print + clean;
    }

    // Segvs on long strings, or usually strings in general
    public static String read(List<AstNode> variables) {
       return variables.stream()
               .map(StdIO::readVariable)
               .collect(Collectors.joining(""));
    }

    private static String readVariable(AstNode variableNode) {
        CType type = IdentifierContext.getType(variableNode.getContentString());
        String fmtFormat = "scanf(\"" + type.toFormat() + "\", &";
        if (type.equals(CType.CSTRING)) {
            fmtFormat = "_builtin_scanstring(&";
        }
        return fmtFormat + variableNode.getContentString() + ");\n";
    }
}
