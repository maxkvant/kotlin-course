package ru.spbau.mit

import org.antlr.v4.runtime.CharStreams
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream


class TestSource {
    fun executeCode(str: String): String {
        val ast = genAst(CharStreams.fromString(str + "\n"))
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
}