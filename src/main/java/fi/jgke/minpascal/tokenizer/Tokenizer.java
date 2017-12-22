package fi.jgke.minpascal.tokenizer;

import fi.jgke.minpascal.Configuration;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.data.Token;
import fi.jgke.minpascal.data.TokenType;
import fi.jgke.minpascal.exception.ParseException;
import lombok.Data;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Tokenizer {
    private Queue<CharacterWithPosition> queue;

    @Data
    public static class CharacterWithPosition {
        private final int character;
        private final Position position;
    }

    public Tokenizer(String content) {
        this.queue = convertQueue(content);
    }

    public Stream<Token> tokenize() {
        Iterator<Token> it = new Iterator<Token>() {
            Optional<Token> lastToken = Optional.empty();

            @Override
            public boolean hasNext() {
                return !lastToken.isPresent() || !lastToken.get().getType().equals(TokenType.EOF);
            }

            @Override
            public Token next() {
                flushWhitespace();
                if (queue.isEmpty()) {
                    lastToken = Optional.of(new Token(TokenType.EOF, Optional.empty(), new Position(-1, -1)));
                    return lastToken.get();
                }

                Position nextPosition = queue.peek().getPosition();

                try {
                    lastToken = Optional.of(parseToken());
                    return lastToken.get();
                } catch (NoSuchElementException e) {
                    throw new ParseException("Unexpected end of file near " + nextPosition);
                }
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.DISTINCT), false);
    }

    private Queue<CharacterWithPosition> convertQueue(String content) {
        int line = 1;
        Queue<CharacterWithPosition> queue = new ArrayDeque<>();
        for (String s : content.split("\n")) {
            int column = 1;
            int length = s.codePointCount(0, s.length()); // UTF-8 :(
            for (int i = 0; i < length; i += Character.charCount(s.codePointAt(i))) {
                int character = s.codePointAt(i);
                queue.add(new CharacterWithPosition(character, new Position(line, column)));
            }
        }
        return queue;
    }

    public static boolean isDigit(CharacterWithPosition c) {
        return c != null && c.getCharacter() >= '0' && c.getCharacter() <= '9';
    }

    public static boolean isLetter(CharacterWithPosition character) {
        if(character == null)
            return false;
        char c = new String(Character.toChars(character.getCharacter())).charAt(0);
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private long parseNumber() {
        long number = 0;
        while (isDigit(queue.peek())) {
            number *= 10;
            number += queue.remove().getCharacter() - '0';
        }
        return number;
    }

    private void flushWhitespace() {
        while (!queue.isEmpty() && new String(Character.toChars(queue.peek().getCharacter())).trim().isEmpty())
            queue.remove();
    }

    private void assertNextDelimiter() {
        CharacterWithPosition next = queue.peek();
        if(isLetter(next)) {
            throw new ParseException(next.getPosition(), "Expected a delimiter");
        }
    }

    private Token parseToken() {
        CharacterWithPosition c = queue.peek();

        if (isDigit(c)) {
            Token t = parseIntOrReal();
            assertNextDelimiter();
            return t;
        }
        throw new ParseException("Not implemented");
    }

    private String cToStr(int codepoint) {
        return new String(Character.toChars(codepoint));
    }

    private int peekNext() {
        return queue.isEmpty() ? 0 : queue.peek().getCharacter();
    }

    private Token parseIntOrReal() {
        Position startingPosition = queue.peek().getPosition();
        long number = parseNumber();
        // Allow numbers like 1e5 if outside strict mode
        if (peekNext() == '.' || (!Configuration.STRICT_MODE && peekNext() == 'e')) {
            long fractional = 0, exponent = 0;
            char sign = '+';
            if (peekNext() == '.')
                queue.remove();
            if (!isDigit(queue.peek()) && Configuration.STRICT_MODE) {
                // Specification forces a number here, but we actually don't need it :)
                CharacterWithPosition nextCharacter = queue.remove();
                throw new ParseException(nextCharacter.getPosition(), String.format(
                        "Invalid character '%s', expected a number",
                        cToStr(nextCharacter.getCharacter())));
            } else if (isDigit(queue.peek())) {
                fractional = parseNumber();
            }
            if (peekNext() == 'e') {
                queue.remove();
                if (peekNext() == '+' || peekNext() == '-') {
                    sign = (char) queue.remove().getCharacter();
                }

                if (!isDigit(queue.peek())) {
                    CharacterWithPosition nextCharacter = queue.remove();
                    throw new ParseException(nextCharacter.getPosition(), String.format(
                            "Invalid character '%s', expected a number",
                            cToStr(nextCharacter.getCharacter())));
                }
                exponent = parseNumber();
            }
            // Let parseDouble handle the math part, so we won't throw away precision unnecessarily
            // roughly (number + fractional / 10^(ceil(log10(fractional)))) * 10^(sign * exponent)
            String doubleString = String.format("%s.%se%s%s", number, fractional, sign, exponent);
            return new Token(TokenType.REAL_LITERAL,
                    Optional.of(Double.parseDouble(doubleString)),
                    startingPosition);
        } else {
            return new Token(TokenType.INTEGER_LITERAL, Optional.of(number), startingPosition);
        }
    }
}
