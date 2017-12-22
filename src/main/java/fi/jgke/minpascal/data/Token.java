package fi.jgke.minpascal.data;

import lombok.Data;
import lombok.NonNull;

import java.util.Optional;

@Data
public class Token {
    @NonNull private final TokenType type;
    @NonNull private final Optional<Object> value;
    @NonNull private final Position position;
}
