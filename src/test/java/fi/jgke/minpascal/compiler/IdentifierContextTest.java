package fi.jgke.minpascal.compiler;

import fi.jgke.minpascal.exception.IdentifierAlreadyExists;
import fi.jgke.minpascal.exception.IdentifierNotFound;
import org.junit.Test;

import static fi.jgke.minpascal.compiler.CType.*;
import static fi.jgke.minpascal.compiler.IdentifierContext.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class IdentifierContextTest {
    @Test
    public void identifierTest() {
        push();
        addIdentifier("a", CDOUBLE);
        push();
        addIdentifier("a", CINTEGER);
        assertThat(getType("a"), is(equalTo(CINTEGER)));
        pop();
        push();
        addIdentifier("a", CBOOLEAN);
        assertThat(getType("a"), is(equalTo(CBOOLEAN)));
        pop();
        assertThat(getType("a"), is(equalTo(CDOUBLE)));
        pop();
    }

    @Test
    public void zerolevel() {
        addIdentifier("foo", CVOID);
        push();
        assertThat(getType("foo"), is(equalTo(CVOID)));
        pop();
    }

    @Test(expected = IdentifierNotFound.class)
    public void identifierNotFound() {
        getType("notfound");
    }

    @Test(expected = IdentifierAlreadyExists.class)
    public void duplicateIdentifier() {
        push();
        addIdentifier("x", CBOOLEAN);
        addIdentifier("x", CBOOLEAN);
    }

    @Test
    public void autoIdentifiers() {
        assertThat("default identifier is _identifier%d", genIdentifier().matches("_identifier\\d+"));
        assertThat("custom label works", genIdentifier("custom").matches("_custom\\d+"));
        String foo = genIdentifier("foo");
        String bar = genIdentifier("foo");
        assertThat(Integer.parseInt(foo.substring(4)) + 1, is(equalTo(Integer.parseInt(bar.substring(4)))));
    }
}
