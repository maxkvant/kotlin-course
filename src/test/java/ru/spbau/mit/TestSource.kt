package ru.spbau.mit

import org.antlr.v4.runtime.CharStreams
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Test
import ru.spbau.mit.Ast.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream


class TestSource {
    private fun astFromString(str: String): Block = genAst(CharStreams.fromString(str + "\n"))

    private fun executeCode(str: String): String {
        val ast = astFromString(str)
        val baos = ByteArrayOutputStream()
        val printStream = PrintStream(baos)
        evaluate(ast, printStream)
        return baos.toString("UTF8")
    }

    @Test
    fun test1() {
        assertThat(executeCode("""|
            | var a = 10
            | var b = 20
            | if (a > b) {
            |   println(1)
            | } else {
            |   println(0)
            | }
        """.trimMargin()), `is`("0\n"))

        assertThat(executeCode("""|
            | fun fib(n) {
            |   if (n <= 1) {
            |     return 1
            |   }
            |   return fib(n - 1) + fib(n - 2)
            | }
            | var i = 1
            | while (i <= 5) {
            |   println(i, fib(i))
            |   i = i + 1
            | }
        """.trimMargin()), `is`("1, 1\n" +
                "2, 2\n" +
                "3, 3\n" +
                "4, 5\n" +
                "5, 8\n"))

        assertThat(executeCode("""|
            | fun foo(n) {
            |   fun bar(m) {
            |     return m + n
            |   }
            |   return bar(1)
            | }
            |
            | println(foo(41))
            |
            |
        """.trimMargin()), `is`("42\n"))
    }

    @Test
    fun functionTest() {
        assertThat(astFromString("""| fun foo() {
                                    | }""".trimMargin()).statements[0],
                   `is`<Statement>(FunctionDef(
                           Identifier("foo"),
                           emptyList(),
                           Block(emptyList(), 1),
                           1)))


        assertThat(astFromString("""| fun bar(n, m) {
                                    |      return n + m
                                    | }""".trimMargin()).statements[0],
                `is`<Statement>(FunctionDef(
                        Identifier("bar"),
                        listOf(Identifier("n"), Identifier("m")),
                        Block(listOf(Return(BinaryOp(VariableCall(Identifier("n"), 2),
                                                     Operation.Plus,
                                                     VariableCall(Identifier("m"), 2),
                                                     2),
                                            2)),
                                     1),
                              1)))

        assertThat(astFromString("""point(1, y)""".trimMargin()).statements[0],
                `is`<Statement>(FunctionCall(
                        Identifier("point"),
                        listOf(Literal(1L, 1),
                               VariableCall(Identifier("y"), 1)),
                        1)))

        assertThat(astFromString("""list()""".trimMargin()).statements[0],
                `is`<Statement>(FunctionCall(
                        Identifier("list"),
                        emptyList(),
                        1)))
    }

    @Test
    fun whileTest() {
        assertThat(astFromString("""while (x < 10) {
            | x = x + 1
            |}""".trimMargin()).statements[0],
                `is`<Statement>(While(
                                BinaryOp(VariableCall(Identifier("x"), 1),
                                         Operation.Lt,
                                         Literal(10, 1), 1),
                                Block(listOf(Assignment(Identifier("x"),
                                                 BinaryOp(VariableCall(Identifier("x"), 2),
                                                          Operation.Plus,
                                                          Literal(1, 2),
                                                 2),
                                             2)),
                                      1),
                                1)))
    }

    @Test
    fun ifTest() {
        assertThat(astFromString("""if (x < 10) {
            | x = x + 1
            |}""".trimMargin()).statements[0],
                `is`<Statement>(If(
                        BinaryOp(VariableCall(Identifier("x"), 1),
                                Operation.Lt,
                                Literal(10, 1), 1),
                        Block(listOf(Assignment(Identifier("x"),
                                BinaryOp(VariableCall(Identifier("x"), 2),
                                        Operation.Plus,
                                        Literal(1, 2),
                                        2),
                                2)),
                                1),
                        null,
                        1)))

        assertThat(astFromString("""if (x < 10) {
            | x = x + 1
            |} else {
            | x = 10
            |}""".trimMargin()).statements[0],
                `is`<Statement>(If(
                        BinaryOp(VariableCall(Identifier("x"), 1),
                                Operation.Lt,
                                Literal(10, 1), 1),
                        Block(listOf(Assignment(Identifier("x"),
                                BinaryOp(VariableCall(Identifier("x"), 2),
                                        Operation.Plus,
                                        Literal(1, 2),
                                        2),
                                2)),
                                1),
                        Block(listOf(Assignment(Identifier("x"), Literal(10, 4), 4)), 3),
                        1)))
    }

    @Test
    fun variableDefTest() {
        assertThat(astFromString("""| var x = 2
                                    |  var y = 1""".trimMargin()),
                `is`<Statement>(Block(listOf(VariableDef(Identifier("x"), Literal(2L, 1), 1),
                                             VariableDef(Identifier("y"), Literal(1L, 2), 2)),
                                      1)))
    }

    @Test
    fun exprTest() {
        assertThat(astFromString("6 / 3 / 2").statements[0], `is`<Statement>(
                BinaryOp(BinaryOp(Literal(6, 1), Operation.Divide, Literal(3, 1),1),
                         Operation.Divide,
                         Literal(2, 1), 1)
        ))
    }
}