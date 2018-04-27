package fi.jgke.minpascal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class OptionalList<T> extends ArrayList<Optional<T>> {
    public OptionalList(Collection<T> collection) {
        super(collection.stream().map(Optional::of).collect(Collectors.toList()));
    }

    @Override
    public Optional<T> get(int i) {
        if(this.size() > i) {
            return this.get(i);
        }

        return Optional.empty();
    }
}
