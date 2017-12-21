package fi.jgke.minpascal.tokenizer;

import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Tokenizer {
    public Stream<Token> tokenize(Queue<Character> content) {
        Token token = parseToken(content);
        if (token.getType().equals(TokenType.EOF)) {
            return Stream.of(token);
        }
        Iterator<Token> it = new Iterator<Token>() {
            Token lastToken = parseToken(content);

            @Override
            public boolean hasNext() {
                return !lastToken.getType().equals(TokenType.EOF);
            }

            @Override
            public Token next() {
                Token token = lastToken;
                lastToken = parseToken(content);
                return token;
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.DISTINCT), false);
    }

    public Token parseToken(Queue<Character> content) {
        return new Token(TokenType.EOF, Optional.empty());
    }
}
