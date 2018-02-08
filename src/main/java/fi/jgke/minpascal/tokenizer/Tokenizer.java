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

import static fi.jgke.minpascal.data.TokenType.*;

public class Tokenizer {
    private ArrayDeque<CharacterWithPosition> queue;

    private static Map<String, TokenType> keywords;
    private static Map<String, TokenType> basicSymbols;

    static {
        keywords = new HashMap<>();
        keywords.put("or", OR);
        keywords.put("and", AND);
        keywords.put("not", NOT);
        keywords.put("if", IF);
        keywords.put("then", THEN);
        keywords.put("else", ELSE);
        keywords.put("of", OF);
        keywords.put("while", WHILE);
        keywords.put("do", DO);
        keywords.put("begin", BEGIN);
        keywords.put("end", END);
        keywords.put("var", VAR);
        keywords.put("array", ARRAY);
        keywords.put("procedure", PROCEDURE);
        keywords.put("function", FUNCTION);
        keywords.put("program", PROGRAM);
        keywords.put("assert", ASSERT);
        keywords.put("return", RETURN);

        basicSymbols = new HashMap<>();
        basicSymbols.put("+", PLUS);
        basicSymbols.put("-", MINUS);
        basicSymbols.put("*", TIMES);
        basicSymbols.put("/", DIVIDE);
        basicSymbols.put("%", MOD);
        basicSymbols.put("=", EQUALS);
        basicSymbols.put("(", OPENPAREN);
        basicSymbols.put(")", CLOSEPAREN);
        basicSymbols.put("[", OPENBRACKET);
        basicSymbols.put("]", CLOSEBRACKET);
        basicSymbols.put(".", DOT);
        basicSymbols.put(",", COMMA);
        basicSymbols.put(";", SEMICOLON);
    }

    @Data
    public static class CharacterWithPosition {
        private final int character;
        private final Position position;
    }

    public Tokenizer(String content) {
        this.queue = convertQueue(content);
    }

    public Stream<Token<?>> tokenize() {
        Iterator<Token<?>> it = new Iterator<Token<?>>() {
            Optional<Token<?>> lastToken = Optional.empty();

            @Override
            public boolean hasNext() {
                return !lastToken.isPresent() || !lastToken.get().getType().equals(EOF);
            }

            @Override
            public Token<?> next() {
                flushWhitespace();
                if (queue.isEmpty()) {
                    lastToken = Optional.of(Token.token(EOF, new Position(-1, -1)));
                    return lastToken.get();
                }

                Position nextPosition = queue.peek().getPosition();

                try {
                    Token<?> token = parseToken();
                    lastToken = Optional.of(token);
                    return token;
                } catch (NoSuchElementException e) {
                    throw new ParseException("Unexpected end of file near " + nextPosition, e);
                }
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.DISTINCT), false);
    }

    private void flushWhitespace() {
        while (!queue.isEmpty() && new String(Character.toChars(queue.peek().getCharacter())).trim().isEmpty())
            queue.remove();
    }

    private ArrayDeque<CharacterWithPosition> convertQueue(String content) {
        int line = 1;
        ArrayDeque<CharacterWithPosition> queue = new ArrayDeque<>();
        for (String s : content.split("\n")) {
            int i;
            for (i = 0; i < s.length(); i += Character.charCount(s.codePointAt(i))) {
                int character = s.codePointAt(i);
                queue.add(new CharacterWithPosition(character, new Position(line, i + 1)));
            }
            queue.add(new CharacterWithPosition('\n', new Position(line, i + 1)));
            line++;
        }
        queue.removeLast(); // Remove last line break
        return queue;
    }

    private Token<?> parseToken() {
        CharacterWithPosition c = queue.peek();

        if (c.getCharacter() == '{') {
            queue.remove();
            parseComment();
            flushWhitespace();
            return parseToken();
        } else if (isDigit(c)) {
            return parseIntOrReal();
        } else if (cToStr(c.getCharacter()).equals("\"") ||
                (!Configuration.STRICT_MODE && cToStr(c.getCharacter()).equals("'"))) {
            return parseString();
        } else if (isLetter(c)) {
            return parseIdentifier();
        }

        return parseOperator();
    }

    private void parseComment() {
        queue.remove(); // *
        //noinspection StatementWithEmptyBody
        while (queue.remove().getCharacter() != '*' || queue.peek().getCharacter() != '}') ;
        queue.remove();
    }

    public static boolean isDigit(CharacterWithPosition c) {
        return c != null && c.getCharacter() >= '0' && c.getCharacter() <= '9';
    }

    public static boolean isLetter(CharacterWithPosition character) {
        if (character == null)
            return false;
        int c = character.getCharacter();
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public static boolean isIdentifierCharacter(CharacterWithPosition c) {
        return c != null && (c.getCharacter() == '_' || isDigit(c) || isLetter(c));
    }

    private int parseNumber() {
        int number = 0;
        while (isDigit(queue.peek())) {
            number *= 10;
            number += queue.remove().getCharacter() - '0';
        }
        return number;
    }

    private String cToStr(int codepoint) {
        return new String(Character.toChars(codepoint));
    }

    private int peekNext() {
        return queue.isEmpty() ? 0 : queue.peek().getCharacter();
    }

    private Token<?> parseIntOrReal() {
        Position startingPosition = queue.peek().getPosition();
        int number = parseNumber();
        // Allow numbers like 1e5 if outside strict mode
        if (peekNext() == '.' || (!Configuration.STRICT_MODE && peekNext() == 'e')) {
            int fractional = 0, exponent = 0;
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
            return Token.token(REAL_LITERAL,
                    Double.parseDouble(doubleString),
                    startingPosition);
        } else {
            return Token.token(INTEGER_LITERAL, number, startingPosition);
        }
    }

    private int getEscapedCharacter(int c) {
        switch (c) {
            case 'n':
                return '\n';
            case 't':
                return '\t';
            case 'r':
                return '\r';
            default:
                return c;
        }
    }

    private Token<String> parseString() {
        // Remove the '"' (or "'")
        CharacterWithPosition limiter = queue.remove();
        Position startingPosition = limiter.getPosition();
        StringBuilder s = new StringBuilder();
        CharacterWithPosition character;

        while ((character = queue.remove()).getCharacter() != limiter.getCharacter()) {
            int c = character.getCharacter();
            if (c == '\\') {
                c = getEscapedCharacter(queue.remove().getCharacter());
            }
            s.append(new String(Character.toChars(c)));
        }

        return Token.token(STRING_LITERAL, s.toString(), startingPosition);
    }

    private Token<?> parseIdentifier() {
        Position startingPosition = queue.peek().getPosition();
        StringBuilder s = new StringBuilder();
        CharacterWithPosition character;

        do {
            character = queue.remove();
            s.append(cToStr(character.getCharacter()));
        } while (isIdentifierCharacter(queue.peek()));

        String identifier = s.toString();

        if (keywords.containsKey(identifier.toLowerCase())) {
            return Token.token(keywords.get(identifier.toLowerCase()), startingPosition);
        }
        return Token.token(IDENTIFIER, identifier, startingPosition);
    }

    private Token<Void> parseOperator() {
        CharacterWithPosition c = queue.remove();
        Position pos = c.getPosition();
        String string = cToStr(c.getCharacter());
        if (basicSymbols.containsKey(string)) {
            return Token.token(basicSymbols.get(string), c.getPosition());
        }
        CharacterWithPosition next = queue.peek();
        switch (cToStr(c.getCharacter())) {
            case "<":
                if (next != null && next.getCharacter() == '>') {
                    queue.remove();
                    return Token.token(NOTEQUALS, pos);
                }
                if (next != null && next.getCharacter() == '=') {
                    queue.remove();
                    return Token.token(LESSTHANEQUALS, pos);
                }
                return Token.token(LESSTHAN, pos);
            case ">":
                if (next != null && next.getCharacter() == '=') {
                    queue.remove();
                    return Token.token(MORETHANEQUALS, pos);
                }
                return Token.token(MORETHAN, pos);
            case ":":
                if (next != null && next.getCharacter() == '=') {
                    queue.remove();
                    return Token.token(ASSIGN, pos);
                }
                return Token.token(COLON, pos);
        }
        throw new ParseException(pos, "Unexpected character '" + cToStr(c.getCharacter()) + "'");
    }
}
