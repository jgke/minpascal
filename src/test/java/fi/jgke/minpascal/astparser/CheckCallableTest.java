package fi.jgke.minpascal.astparser;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class CheckCallableTest {
    private List<Consumer<?>> calledConsumers;
    private List<Function<?, ?>> calledFunctions;

    @SuppressWarnings("unchecked")
    private <T> Consumer<T> spyConsumer(Consumer<T> consumer) {
        @SuppressWarnings("unchecked")
        Consumer<T> spy = (Consumer<T>) Mockito.spy(Consumer.class);
        Mockito.doAnswer(it -> {
            T item = (T) it.getArguments()[0];
            // Pass it to the real consumer so it gets processed.
            consumer.accept(item);
            return null;
        }).when(spy).accept(Mockito.any((Class<T>) Object.class));
        return spy;
    }

    @SuppressWarnings("unchecked")
    private <T, R> Function<T, R> spyFunction(Function<T, R> function) {
        @SuppressWarnings("unchecked")
        Function<T, R> spy = (Function<T, R>) Mockito.spy(Function.class);
        Mockito.doAnswer(it -> {
            T item = (T) it.getArguments()[0];
            // Pass it to the real consumer so it gets processed.
            return function.apply(item);
        }).when(spy).apply(Mockito.any((Class<T>) Object.class));
        return spy;
    }

    @Before
    public void initCalledList() {
        calledConsumers = new ArrayList<>();
        calledFunctions = new ArrayList<>();
    }

    @After
    public void checkCalled() {
        calledConsumers.forEach(thing -> Mockito.verify(thing).accept(Mockito.any()));
    }

    public <A> Consumer<A> calledConsumer(Consumer<A> fn) {
        fn = spyConsumer(fn);
        calledConsumers.add(fn);
        return fn;
    }

    public <A, B> Function<A, B> calledFunction(Function<A, B> fn) {
        fn = spyFunction(fn);
        calledFunctions.add(fn);
        return fn;
    }
}
