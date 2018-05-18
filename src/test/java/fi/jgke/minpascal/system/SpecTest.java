package fi.jgke.minpascal.system;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static fi.jgke.minpascal.system.CTest.testCompiledOutput;

public class SpecTest {
    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);

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
                "    return x; " +
                "end " +
                "function baz (z : integer): integer; " +
                "begin " +
                "    return 5 + z; " +
                "end " +
                "function bar (var y : integer): integer; " +
                "begin " +
                "    y := baz(y); " +
                "    return y; " +
                "end " +
                "procedure qux (var y : integer); " +
                "begin " +
                "    y := bar(y); " +
                "end " +
                "begin " +
                "  var x: integer;" +
                "  x := 5;" +
                "  var y: integer;" +
                "  y := 5;" +
                "  y := foo(y);" +
                "  WriteLn(y);" +
                "  qux(y);" +
                "  WriteLn(y);" +
                "  WriteLn(x);" +
                "  foo(x);" +
                "  WriteLn(x);" +
                "  bar(x);" +
                "  WriteLn(x);" +
                "end.";
        testCompiledOutput(s, "10\n15\n5\n5\n10\n");
    }

    /*
     * Mini-Pascal includes a C-style assert statement. If an assertion fails
     * the system prints out a diagnostic message and halts execution.
     */
    @Test
    public void three() {
        String s = "program Hello; " +
                "begin " +
                "  assert(1 > 2);" +
                "end.";
        testCompiledOutput(s, "", "Assertion failed\n", 1);
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

    /*
     * 5. By default, variables in Pascal are not initialized (with zero or
     * otherwise); so they may initially contain rubbish values.
     *    -> doesn't apply on this implementation, everything is initialized
     */

    /*
     * A Mini-Pascal program can print numbers and strings via the predefined
     * special routines read and writeln. The stream-style IO makes conversion
     * of values from their text representation to their internal numerical
     * (binary) representation.
     */
    @Test
    public void five() {
        String s = "program Hello; " +
                "begin " +
                "  var x : integer;" +
                "  read(x);" +
                "  writeln(x);" +
                "  var y : string;" +
                "  read(y);" +
                "  writeln(y);" +
                "end.";
        testCompiledOutput(s, "5\nfoo\n", "5\nfoo\n", 0);
    }

    /*
     * Pascal is a case non-sensitive language, which means you can write the
     * names of variables, functions and procedures in either case.
     */
    @Test
    public void six() {
        String s = "program Hello; " +
                "function baz (z : integer): integer; " +
                "begin " +
                "    return 5 + z; " +
                "end " +
                "begin " +
                "  var foo : integer;" +
                "  Foo := 5;" +
                "  writeln(FOO);" +
                "  writeln(BAZ(10));" +
                "end.";
        testCompiledOutput(s, "5\n15\n");
    }

    /*
     *  The Mini-Pascal multiline comments are enclosed within curly brackets
     *  and asterisks as follows: "{* . . . *}".
     */
    @Test
    public void seven() {
        String s = "program Hello; " +
                "begin " +
                "  writeln(1);" +
                "{*" +
                "  writeln(2);" + // not printed
                "*}" +
                "end.";
        testCompiledOutput(s, "1\n");
    }

    /*
    * Note that the names Boolean, false, integer, read, real, size, string,
    * true, writeln are treated in Mini-Pascal as "predefined identifiers",
    * i.e., it is allowed to use them as regular identifiers in MiniPascal
    * programs.
    *
    * This is bugged here -- read is handled specially in the parser so it
    * cannot be used as a lvalue, however it can be passed to the read
    * function :).
    */
    @Test
    public void eight() {
        String s = "program Hello; " +
                "begin " +
                "  var read: integer;" +
                "  begin" +
                "    read(read);" +
                "    writeln(read);" +
                "  end " +
                "end.";
        testCompiledOutput(s, "5\n", "5\n", 0);
    }
}
