package fi.jgke.minpascal.parser.nodes;

import fi.jgke.minpascal.data.TreeNode;
import fi.jgke.minpascal.util.OptionalUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;
import java.util.function.Function;

@Data
@EqualsAndHashCode(callSuper = true)
public class LiteralNode extends TreeNode {
    private final Optional<Integer> integer;
    private final Optional<Double> number;
    private final Optional<String> string;
    private final Optional<Boolean> bool;

    public <T> T map(
            Function<Integer, T> integer,
            Function<Double, T> number,
            Function<String, T> string,
            Function<Boolean, T> bool
    ) {
        return OptionalUtils.<T>until()
                .chain(this.integer, integer)
                .chain(this.number, number)
                .chain(this.string, string)
                .chain(this.bool, bool)
                .get();
    }
}
