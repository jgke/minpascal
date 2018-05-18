package fi.jgke.minpascal.compiler.std;

import com.google.common.collect.Streams;
import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.compiler.CType;
import fi.jgke.minpascal.compiler.IdentifierContext;
import fi.jgke.minpascal.exception.TypeError;
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
        return result -> {
            sign.ifPresent(s -> {
                CType type = result.getType();
                if (!type.equals(CType.CINTEGER) &&
                        !type.equals(CType.CDOUBLE))
                    throw new TypeError("Expected integer or real, got " + type.formatType());
                String id = genIdentifier();
                result.temporaries.add(type.toDeclaration(id, Optional.empty()) + " = " + s + " " + result.getIdentifier());
                result.identifier = id;
            });
            return result;
        };
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
                .map("op", p -> CExpressionResult.fromExpression(p.getFirstChild("Expression")))
                .map("not", CExpressionResult::fromNot)
                .unwrap();
        if (post.isPresent()) {
            String identifier = unwrap.getIdentifier();
            CType type = unwrap.getType().getPtrTo()
                    .orElseThrow(() -> new TypeError("Expected " + identifier + " to be an array but it isn't"));
            String s = post.get();
            String tmp = genIdentifier();
            ArrayList<String> temps = new ArrayList<>(unwrap.getTemporaries());
            temps.add(type.toDeclaration(tmp, Optional.empty()) + " = " + identifier + s + ";");
            unwrap = new CExpressionResult(type, tmp, temps, unwrap.getPost());
        }
        return unwrap;
    }

    private static CExpressionResult fromNot(AstNode astNode) {
        CExpressionResult factor = fromFactor(astNode.getFirstChild("Factor"));
        if(!factor.getType().equals(CType.CBOOLEAN)) {
            throw new TypeError("Expected boolean but got " + factor.getType().formatType());
        }
        String identifier = factor.getIdentifier();
        factor.getTemporaries().add(identifier + " = !" + identifier + ";");
        return factor;
    }

    private static CExpressionResult fromVariable(AstNode astNode) {
        String identifier = astNode
                .getFirstChild("Variable")
                .getFirstChild("identifier").getContentString();
        Optional<AstNode> arrayIndex = astNode.getFirstChild("Variable")
                .getFirstChild("ob").toOptional();
        CType type = IdentifierContext.getType(identifier);
        String realName = IdentifierContext.getRealName(identifier);
        Function<CExpressionResult, CExpressionResult> addPre = Function.identity();
        if (arrayIndex.isPresent()) {
            AstNode idx = arrayIndex.get();
            String finalIdentifier1 = identifier;
            String newIdentifier = genIdentifier();
            type = type.getPtrTo()
                    .orElseThrow(() -> new TypeError("Expected " + finalIdentifier1 + " to be an array but it isn't"));
            IdentifierContext.addIdentifier(newIdentifier, type);
            CExpressionResult cExpressionResult = fromExpression(idx.getFirstChild("ob").getFirstChild("Expression"));
            identifier = newIdentifier;
            realName = newIdentifier;
            CType finalType1 = type;
            addPre = o -> {
                cExpressionResult.getTemporaries().add(finalType1.toDeclaration(newIdentifier, Optional.empty()) + " = " + finalIdentifier1 + "[" + cExpressionResult.getIdentifier() + "];");
                o.getTemporaries().addAll(0, cExpressionResult.getTemporaries());
                o.getPost().addAll(cExpressionResult.getPost());
                return o;
            };
        }
        CType finalType = type;
        String finalIdentifier = identifier;
        String finalRealName = realName;
        return addPre.apply(astNode.getFirstChild("Arguments").toOptional()
                .map(args -> fromCall(finalIdentifier, finalType, args))
                .orElseGet(() -> new CExpressionResult(finalType,
                        finalRealName,
                        Collections.emptyList(),
                        Collections.emptyList())));
    }

    private static String getSign(AstNode sign) {
        return sign.<String>toMap()
                .map("plus", $ -> "+")
                .map("minus", $ -> "-")
                .unwrap();
    }

    private static CExpressionResult fromCall(String identifier, CType type, AstNode call) {
        CType returnType = type.getReturnType()
                .orElseThrow(() -> new TypeError(identifier + " isn't a function"));
        List<CExpressionResult> expressions = CExpressionResult.getArguments(call.getFirstChild("Arguments"));
        List<String> temporaries = expressions.stream()
                .flatMap(e -> e.getTemporaries().stream())
                .collect(Collectors.toList());
        List<String> post = expressions.stream()
                .flatMap(e -> e.getPost().stream())
                .collect(Collectors.toList());
        if (type.getParameters().size() != expressions.size()) {
            throw new TypeError("Invalid arity: Got " + expressions.size() + " parameters but expected " + type.getParameters().size());
        }
        String arguments =
                Streams.zip(expressions.stream(), type.getParameters().stream(),
                        (a, b) -> a.getType().assignTo(b, a.getIdentifier()))
                        .collect(Collectors.joining(","));
        String result = genIdentifier();
        temporaries.add(returnType.toDeclaration(result, Optional.empty()) + " = " + IdentifierContext.getRealName(identifier) + "(" + arguments + ");");
        return new CExpressionResult(returnType, result, temporaries, post);
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
