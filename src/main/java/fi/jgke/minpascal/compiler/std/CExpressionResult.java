package fi.jgke.minpascal.compiler.std;

import com.google.common.collect.Streams;
import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.compiler.IdentifierContext;
import fi.jgke.minpascal.exception.CompilerException;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jgke.minpascal.compiler.IdentifierContext.genIdentifier;

@Data
public class CExpressionResult {
    private final CType type;
    private String identifier;
    private final List<String> temporaries;
    private final List<String> post;

    public CExpressionResult(CType type, String identifier, List<String> temporaries, List<String> post) {
        this.type = type;
        this.identifier = identifier;
        this.temporaries = new ArrayList<>(temporaries);
        this.post = new ArrayList<>(post);
    }

    public static CExpressionResult fromExpression(AstNode arg) {
        AstNode left = arg.getFirstChild("SimpleExpression");
        AstNode relOp = arg.getFirstChild("RelOp");
        return relOp.toOptional().map(rel ->
                getType(fromSimple(left),
                        getRelOp(rel),
                        fromSimple(rel.getFirstChild("RelOp").getFirstChild("SimpleExpression")))
        ).orElse(fromSimple(left));
    }

    private static String getAddOp(AstNode node) {
        return node.getFirstChild("AddOp").getFirstChild("AddOp")
                .<String>toMap()
                .map("plus", AstNode::getContentString)
                .map("minus", AstNode::getContentString)
                .map("or", AstNode::getContentString)
                .unwrap();
    }

    private static String getRelOp(AstNode node) {
        return node.getFirstChild("RelOp").getFirstChild("RelOp")
                .<String>toMap()
                .map("lessthanequals", AstNode::getContentString)
                .map("morethanequals", AstNode::getContentString)
                .map("lessthan", AstNode::getContentString)
                .map("morethan", AstNode::getContentString)
                .map("notequals", AstNode::getContentString)
                .map("equals", AstNode::getContentString)
                .unwrap();
    }

    private static CExpressionResult fromSimple(AstNode simple) {
        simple = simple.getFirstChild("Sign");
        Optional<String> sign = simple.getFirstChild("Sign")
                .toOptional()
                .map(CExpressionResult::getSign);
        AstNode left = simple.getFirstChild("Term");
        return left.getFirstChild("AddOp").toOptional()
                .map(add -> getType(
                        addSign(sign).apply(fromTerm(left.getFirstChild("Term"))),
                        getAddOp(add),
                        fromTerm(add.getFirstChild("AddOp").getFirstChild("Term"))
                )).orElse(addSign(sign).apply(fromTerm(left.getFirstChild("Term"))));
    }

    private static CExpressionResult getType(CExpressionResult left, String op, CExpressionResult right) {
        return CBinaryExpressions.apply(left, CBinaryExpressionFnPrivate.getOperator(op), right);
    }

    private static Function<CExpressionResult, CExpressionResult> addSign(Optional<String> sign) {
        return result -> sign.map(s -> {
            CType type = result.getType();
            if (!type.equals(CType.CINTEGER) &&
                    !type.equals(CType.CDOUBLE))
                throw new CompilerException("Type error");
            String id = genIdentifier();
            result.temporaries.add(type.toDeclaration(id, Optional.empty()) + " = " + s + " " + result.getIdentifier());
            result.identifier = id;
            return result;
        }).orElse(result);
    }

    private static String getMulOp(AstNode node) {
        return node.getFirstChild("MulOp").getFirstChild("MulOp")
                .<String>toMap()
                .map("times", AstNode::getContentString)
                .map("mod", AstNode::getContentString)
                .map("divide", AstNode::getContentString)
                .map("and", AstNode::getContentString)
                .unwrap();
    }

    private static CExpressionResult fromTerm(AstNode left) {
        AstNode relOp = left.getFirstChild("MulOp");
        return relOp.toOptional().map(rel ->
                getType(fromFactor(left.getFirstChild("Factor")),
                        getMulOp(rel),
                        fromFactor(rel.getFirstChild("MulOp").getFirstChild("Factor")))
        ).orElse(fromFactor(left.getFirstChild("Factor")));
    }

    private static CExpressionResult fromFactor(AstNode factor) {
        Optional<String> post = factor.getFirstChild("SizeExpression")
                .toOptional()
                .map($ -> "[-1]");
        AstNode subFactor = factor.getFirstChild("SubFactor");
        CExpressionResult unwrap = subFactor.<CExpressionResult>toMap()
                .map("Variable", CExpressionResult::fromVariable)
                .map("Literal", CExpressionResult::getLiteralType)
                .map("op", notImplemented())
                .map("not", notImplemented())
                .unwrap();
        if (post.isPresent()) {
            CType type = unwrap.getType().getPtrTo().get();
            String s = post.get();
            String tmp = genIdentifier();
            ArrayList<String> temps = new ArrayList<>(unwrap.getTemporaries());
            temps.add(type.toDeclaration(tmp, Optional.empty()) + " = " + unwrap.getIdentifier().substring(1) + s + ";");
            unwrap = new CExpressionResult(type, tmp, temps, unwrap.getPost());
        }
        return unwrap;
    }

    private static CExpressionResult fromVariable(AstNode astNode) {
        String identifier = astNode
                .getFirstChild("Variable")
                .getFirstChild("identifier").getContentString();
        return astNode.getFirstChild("Arguments").toOptional()
                .map(args -> fromCall(identifier, args))
                .orElseGet(() -> new CExpressionResult(IdentifierContext.getType(identifier),
                        IdentifierContext.getRealName(identifier),
                        Collections.emptyList(),
                        Collections.emptyList()));
    }

    private static Function<AstNode, CExpressionResult> notImplemented() {
        return $ -> {
            throw new RuntimeException("not impl " + $);
        };
    }

    private static String getSign(AstNode sign) {
        return sign.<String>toMap()
                .map("plus", $ -> "+")
                .map("minus", $ -> "-")
                .unwrap();
    }

    private static CExpressionResult fromCall(String identifier, AstNode call) {
        List<CExpressionResult> expressions = CExpressionResult.getArguments(call.getFirstChild("Arguments"));
        List<String> temporaries = expressions.stream()
                .flatMap(e -> e.getTemporaries().stream())
                .collect(Collectors.toList());
        List<String> post = expressions.stream()
                .flatMap(e -> e.getPost().stream())
                .collect(Collectors.toList());
        String arguments =
                Streams.zip(expressions.stream(), IdentifierContext.getType(identifier).getParameters().stream(),
                        (a, b) -> b.getPtrTo().map(to ->
                                a.getType().getPtrTo().map($ -> a.getIdentifier())
                                        .orElse("&" + a.getIdentifier()))
                                .orElse(a.getIdentifier()))
                        .collect(Collectors.joining(", "));
        String result = genIdentifier();
        CType type = IdentifierContext.getType(identifier).getReturnType()
                .orElseThrow(() -> new CompilerException("Identifier not found"));
        temporaries.add(type.toDeclaration(result, Optional.empty()) + " = " + IdentifierContext.getRealName(identifier) + "(" + arguments + ");");
        return new CExpressionResult(type, result, temporaries, post);
    }

    private static CExpressionResult toExpression(CType type, Object value) {
        String id = genIdentifier();
        return new CExpressionResult(type, id, Collections.singletonList(type.toDeclaration(id, Optional.empty()) + " = " + value + ";"),
                Collections.emptyList());
    }

    private static CExpressionResult getLiteralType(AstNode literalNode) {
        return literalNode.<CExpressionResult>toMap()
                .map("realliteral", d -> toExpression(CType.CDOUBLE, Double.parseDouble(d.getContentString())))
                .map("integerliteral", d -> toExpression(CType.CINTEGER, Integer.parseInt(d.getContentString())))
                .map("stringliteral", d -> toExpression(CType.CSTRING, "_builtin_strdup(" + d.getContentString() + ")"))
                .unwrap();
    }

    public static String formatExpressions(List<CExpressionResult> expressions,
                                           Function<List<CExpressionResult>, Object> core) {
        List<List<String>> steps = new ArrayList<>();
        List<String> post = new ArrayList<>();

        for (CExpressionResult result : expressions) {
            steps.add(result.getTemporaries());
            post.addAll(result.getPost());
        }

        String pre = steps.stream()
                .map(list -> list.stream()
                        .collect(Collectors.joining("\n")) + "\n")
                .collect(Collectors.joining("\n")) + "\n";
        String clean = post.stream().collect(Collectors.joining("\n")) + "\n";
        return pre + core.apply(expressions) + clean;
    }

    public static List<CExpressionResult> getArguments(AstNode argumentsNode) {
        Optional<AstNode> astNode = argumentsNode.getFirstChild("Expression")
                .getFirstChild("Expression").toOptional();
        return astNode.map(args -> Stream.concat(Stream.of(args.getFirstChild("Expression")),
                args.getFirstChild("more")
                        .getList().stream()
                        .map(m -> m.getFirstChild("Expression")))
                .map(CExpressionResult::fromExpression)
                .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
