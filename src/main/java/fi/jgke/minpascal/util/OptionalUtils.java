package fi.jgke.minpascal.util;

import fi.jgke.minpascal.exception.CompilerException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OptionalUtils {
    public static class Until<T> {
        Optional<T> optional;

        public Until() {
            this.optional = Optional.empty();
        }

        public <U> Until<T> chain(Optional<U> alternative, Function<U, T> fn) {
            if (!optional.isPresent()) {
                optional = alternative.map(fn);
            }
            return this;
        }

        public T get() {
            return optional.orElse(null);
        }
    }

    public static <T> Until<T> until() {
        return new Until<>();
    }

    public static void assertOne(Optional<?>... optionals) {
        int count = Arrays.stream(optionals)
                .mapToInt(o -> o.map($ -> 1).orElse(0))
                .sum();

        if(count != 1) {
            throw new CompilerException("asserted one optional but got none or multiple");
        }
    }

    public static <T> List<T> toList(Optional<T> ...optionals) {
        return Arrays.stream(optionals)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
