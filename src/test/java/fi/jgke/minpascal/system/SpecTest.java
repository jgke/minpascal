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

    /*
     * A var parameter is passed by reference, i.e. its address is passed, and
     * inside the subroutine the parameter name acts as a synonym for the
     * variable given as an argument. A called procedure or function can freely
     * read and write a variable that the caller passed in the argument list.
     */
    @Test
    public void two() {
        String s = "program Hello; " +
                "function foo (x : integer): integer; " +
                "begin " +
                "    x :=  5 + x; " +
                "end " +
                "function baz (z : integer): integer; " +
                "begin " +
                "    return 5 + z; " +
                "end " +
                "function bar (var y : integer): integer; " +
                "begin " +
                "    y :=  baz(y); " +
                "end " +
                "begin " +
                "  var x: integer;" +
                "  x := 5;" +
                "  WriteLn(x);" +
                "  foo(x);" +
                "  WriteLn(x);" +
                "  bar(x);" +
                "  WriteLn(x);" +
                "end.";
        testCompiledOutput(s, "5\n5\n10\n");
    }

    /*
     * Mini-Pascal includes a C-style assert statement. If an assertion fails
     * the system prints out a diagnostic message and halts execution.
     */
    @Test
    public void three() {
        String s = "program Hello; " +
                "begin " +
                "  assert(false);" +
                "end.";
        testCompiledOutput(s, "Assertion failed\n", 1);
    }

    /*
     * The Mini-Pascal operation a.size only applies to values of type array
     * of T (where T is a simple type). There are only one-dimensional arrays.
     * Array types are compatible only if they have the same element type.
     * Arrays' indices begin with zero. The compatibility of array
     * indices and array sizes is checked at run time (usually).
     */
    @Test
    public void four() {
        String s = "program Hello; " +
                "procedure foo (x: array [] of integer); " +
                "  begin " +
                "    writeln (x[0], x.size); " +
                "  end " +
                "begin " +
                "  var x : array [5] of integer;" +
                "  x[0] := 4;" +
                "  foo(x);" +
                "end.";
        testCompiledOutput(s, "4 5\n");
    }
}
