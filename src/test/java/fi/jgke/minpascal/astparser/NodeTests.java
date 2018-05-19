package fi.jgke.minpascal.astparser;

import fi.jgke.minpascal.astparser.nodes.LeafNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.astparser.parsers.RuleParser;
import fi.jgke.minpascal.data.Position;
import fi.jgke.minpascal.exception.CompilerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static fi.jgke.minpascal.astparser.ParserTestUtils.initRules;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NodeTests {
    private List<Consumer<?>> calledConsumers;
    private List<Function<?, ?>> calledFunctions;
    private final Position p = new Position(0, 0);

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
        calledFunctions.forEach(thing -> Mockito.verify(thing).apply(Mockito.any()));
    }

    private <A> Consumer<A> calledConsumer(Consumer<A> fn) {
        fn = spyConsumer(fn);
        calledConsumers.add(fn);
        return fn;
    }

    private <A, B> Function<A, B> calledFunction(Function<A, B> fn) {
        fn = spyFunction(fn);
        calledFunctions.add(fn);
        return fn;
    }

    @Test
    public void listNodeTests() {
        LeafNode leafNode = new LeafNode("bar", "baz", p);
        ListAstNode listAstNode = new ListAstNode("foo", Collections.singletonList(leafNode), p);
        listAstNode.setAvailableNames(Collections.singleton("bar"));
        assertThat(listAstNode.getList().size(), is(equalTo(1)));
        assertThat(listAstNode.getList().get(0), is(equalTo(leafNode)));
        listAstNode.toMap()
                .chain("bar", calledConsumer($ -> {
                }))
                .unwrap();
        listAstNode.toMap()
                .map("bar", calledFunction($ -> $))
                .unwrap();

        listAstNode = new ListAstNode("foo", Collections.singletonList(leafNode), p);
        listAstNode.setAvailableNames(new HashSet<>(Arrays.asList("bar", "qux")));
        assertThat(listAstNode.getList().size(), is(equalTo(1)));
        assertThat(listAstNode.getList().get(0), is(equalTo(leafNode)));
        listAstNode.toMap()
                .chain("bar", calledConsumer($ -> {
                }))
                .chain("qux", $ -> assertThat("Unreachable", false))
                .unwrap();
        listAstNode.toMap()
                .map("bar", calledFunction($ -> $))
                .map("qux", $ -> {
                    assertThat("Unreachable", false);
                    return $;
                })
                .unwrap();
    }

    @Test(expected = AssertionError.class)
    public void listMapFails() {
        LeafNode leafNode = new LeafNode("bar", "baz", p);
        new ListAstNode("foo", Collections.singletonList(leafNode), p)
                .toMap()
                .chain("bar", $ -> {
                })
                .unwrap();
    }

    @Test(expected = CompilerException.class)
    public void incompleteMap() {
        LeafNode leafNode = new LeafNode("bar", "baz", p);
        ListAstNode foo = new ListAstNode("foo", Collections.singletonList(leafNode), p);
        foo.setAvailableNames(new HashSet<>(Arrays.asList("foo", "bar")));
        foo.toMap()
                .chain("bar", $ -> {
                })
                .unwrap();
    }

    @Test(expected = CompilerException.class)
    public void invalidChainCall() {
        LeafNode leafNode = new LeafNode("bar", "baz", p);
        new ListAstNode("foo", Collections.singletonList(leafNode), p)
                .setAvailableNames(new HashSet<>(Arrays.asList("foo", "bar")))
                .toMap()
                .chain("foo", $ -> {
                })
                .chain("bag", $ -> {
                })
                .unwrap();
    }

    @Test(expected = CompilerException.class)
    public void invalidMapCall() {
        LeafNode leafNode = new LeafNode("bar", "baz", p);
        ListAstNode foo = new ListAstNode("foo", Collections.singletonList(leafNode), p);
        foo.setAvailableNames(new HashSet<>(Arrays.asList("foo", "bar")));
        foo.toMap()
                .map("foo", $ -> $)
                .map("bag", $ -> $)
                .unwrap();
    }

    @Test
    public void unwrapMapCall() {
        LeafNode leafNode = new LeafNode("bar", "baz", p);
        ListAstNode foo = new ListAstNode("Foo", Collections.singletonList(leafNode), p);
        new ListAstNode("Foo", Collections.singletonList(foo), p)
                .setAvailableNames(new HashSet<>(Arrays.asList("Foo", "bar")))
                .toMap()
                .chain("Foo", f -> assertThat(f.getFirstChild("bar").getContentString(), is(equalTo("baz"))))
                .map("bar", $ -> $)
                .unwrap();
    }

    @Test(expected = CompilerException.class)
    public void invalidMap() {
        initRules("X ::= a | b",
                  "a ::= 'a'",
                  "b ::= 'b'");
        new RuleParser("X").parse("a").getLeft().toMap()
                .map("a", $ -> null)
                .map("c", $ -> null)
                .unwrap();
    }
}
