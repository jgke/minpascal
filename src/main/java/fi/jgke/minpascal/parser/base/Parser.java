package fi.jgke.minpascal.parser.base;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.parser.blocks.Program;
import fi.jgke.minpascal.parser.nodes.BlockNode;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {
    public BlockNode parse(Stream<Token<?>> tokenStream) {
        return new Program().parse(tokenStream.collect(Collectors.toCollection(ParseQueue::new)));
    }
}
