package fi.jgke.minpascal.data;

import fi.jgke.minpascal.util.Formatter;
import lombok.Data;

@Data
public class TreeNode {
    public void debug() {
        System.out.println(Formatter.formatTree(this.toString()));
    }
}
