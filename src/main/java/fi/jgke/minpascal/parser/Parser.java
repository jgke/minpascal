package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TreeNode;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {
    public TreeNode parse(Stream<Token> tokenStream) {
        return new Program().parse(tokenStream.collect(Collectors.toCollection(ParseQueue::new)));
    }
}
