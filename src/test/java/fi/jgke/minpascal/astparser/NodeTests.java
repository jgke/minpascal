package fi.jgke.minpascal.astparser;

import fi.jgke.minpascal.astparser.nodes.LeafNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import fi.jgke.minpascal.exception.CompilerException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NodeTests extends CheckCallableTest {
    @Test
    public void listNodeTests() {
        LeafNode leafNode = new LeafNode("bar", "baz");
        ListAstNode listAstNode = new ListAstNode("foo", Collections.singletonList(leafNode));
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

        listAstNode = new ListAstNode("foo", Collections.singletonList(leafNode));
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
        LeafNode leafNode = new LeafNode("bar", "baz");
        new ListAstNode("foo", Collections.singletonList(leafNode))
                .toMap()
                .chain("bar", $ -> {
                })
                .unwrap();
    }

    @Test(expected = CompilerException.class)
    public void incompleteMap() {
        LeafNode leafNode = new LeafNode("bar", "baz");
        ListAstNode foo = new ListAstNode("foo", Collections.singletonList(leafNode));
        foo.setAvailableNames(new HashSet<>(Arrays.asList("foo", "bar")));
        foo.toMap()
                .chain("bar", $ -> {
                })
                .unwrap();
    }
}
