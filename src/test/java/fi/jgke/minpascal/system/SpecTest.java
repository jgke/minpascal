package fi.jgke.minpascal.system;

import org.junit.Test;

import static fi.jgke.minpascal.system.CTest.testCompiledOutput;

public class SpecTest {
    /* A Mini-Pascal program consist of series of functions and procedures,
     * and a main block. The subroutines may call each other and may be
     * (mutually) recursive. Within the same scope (procedure, function, or
     * block), identifiers must be unique but it is OK to redefine a name in
     * an inner scope. */
    @Test
    public void one() {
        String s = "program Hello; " +
                "  procedure foo (); " +
                "  begin " +
                "    writeln (\"Procedure call\"); " +
                "  end " +
                "  function bar (x : integer): integer; " +
                "  begin " +
                "    writeln (\"Function call\"); " +
                "    return 5 + x; " +
                "  end " +
                " begin " +
                "    writeln (bar(5)); " +
                "  end.";
        testCompiledOutput(s, "Function call\n10\n");
    }
}
