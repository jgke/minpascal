package fi.jgke.minpascal.parser.blocks;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.parser.base.Parsable;
import fi.jgke.minpascal.parser.base.ParseQueue;
import fi.jgke.minpascal.parser.nodes.ArrayTypeNode;
import fi.jgke.minpascal.parser.nodes.SimpleTypeNode;
import fi.jgke.minpascal.parser.nodes.TypeNode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Type implements Parsable {
    private final Parsable[] children = new Parsable[]{new SimpleType(), new ArrayType()};

    @Override
    public List<Parsable> getParsables() {
        return Arrays.asList(children);
    }

    @Override
    public TypeNode parse(ParseQueue queue) {
        TreeNode type = queue.any(children);

        if (type instanceof SimpleTypeNode) {
            return new TypeNode(Optional.of((SimpleTypeNode) type), Optional.empty());
        }
        return new TypeNode(Optional.empty(), Optional.of((ArrayTypeNode) type));
    }
}
