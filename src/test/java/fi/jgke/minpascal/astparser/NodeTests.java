package fi.jgke.minpascal.astparser;

import fi.jgke.minpascal.astparser.nodes.AstNode;
import fi.jgke.minpascal.astparser.nodes.LeafNode;
import fi.jgke.minpascal.astparser.nodes.ListAstNode;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NodeTests extends CheckCallableTest {
    private Consumer<AstNode> spyConsumer(Consumer<AstNode> consumer) {
        @SuppressWarnings("unchecked")
        Consumer<AstNode> spy = (Consumer<AstNode>) Mockito.spy(Consumer.class);
        Mockito.doAnswer(it -> {
            AstNode item = (AstNode) it.getArguments()[0];
            // Pass it to the real consumer so it gets processed.
            consumer.accept(item);
            return null;
        }).when(spy).accept(Mockito.any(AstNode.class));
        return spy;
    }

    @Test
    public void listNodeTests() {
        LeafNode leafNode = new LeafNode("bar", "baz");
        ListAstNode listAstNode = new ListAstNode("foo", Collections.singletonList(leafNode));
        listAstNode.setAvailableNames(Collections.singleton("bar"));
        assertThat(listAstNode.getList().size(), is(equalTo(1)));
        assertThat(listAstNode.getList().get(0), is(equalTo(leafNode)));
        listAstNode.toMap()
                .chain("bar", calledConsumer($ -> {}))
                .unwrap();
    }
}
