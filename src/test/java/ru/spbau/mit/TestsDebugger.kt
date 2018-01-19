package ru.spbau.mit

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import ru.spbau.mit.ast.Identifier
import ru.spbau.mit.ast.VariableCall
import ru.spbau.mit.repl.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class TestsDebugger {
    private fun ByteArrayOutputStream.getString(): String {
        val res = this.toString("UTF8")
        this.reset()
        return res
    }

    private fun initExecutor(): Pair<Executor, ByteArrayOutputStream> {
        val baos = ByteArrayOutputStream()
        val printStream = PrintStream(baos)
        return Pair(Executor(printStream), baos)
    }

    @Test
    fun toReplCommandTest() {
        assertThat("list".toReplCommand(), `is`<Command>(ListBreakpoints))
        assertThat("run".toReplCommand(), `is`<Command>(Run))
        assertThat(" stop".toReplCommand(), `is`<Command>(Stop))
        assertThat("continue ".toReplCommand(), `is`<Command>(Continue))
        assertThat("breakpoint 3".toReplCommand(), `is`<Command>(Breakpoint(3)))
        assertThat("condition 2 a > 10".toReplCommand(), `is`<Command>(Breakpoint(2, "a > 10")))
        assertThat("remove 3".toReplCommand(), `is`<Command>(RemoveBreakpoint(3)))
    }

    @Test
    fun executorTest() {
        val (executor1, baos1) = initExecutor()
        val ast1 = astFromString("""| var x = 0
                                    | while (x < 5) {
                                    |   println(x)
                                    |   x = x + 1
                                    | }
                                    | x = 100
                                    | var a
                                    | """.trimMargin())

        executor1.onCommand(Load(ast1))
        assertThat(baos1.getString(), `is`("loaded\n"))
        executor1.onCommand(Breakpoint(4))
        assertThat(baos1.getString(), `is`(""))

        executor1.onCommand(ListBreakpoints)
        assertThat(baos1.getString(), `is`("[Breakpoint(line=4, exprStr=1)]\n"))

        executor1.onCommand(Run)
        assertThat(baos1.getString(), `is`("""|0
                                              |stopped on line 4
                                              |""".trimMargin()))

        executor1.onCommand(Continue)
        assertThat(baos1.getString(), `is`("""|1
                                              |stopped on line 4
                                              |""".trimMargin()))

        executor1.onCommand(RemoveBreakpoint(4))
        executor1.onCommand(ListBreakpoints)
        assertThat(baos1.getString(), `is`("[]\n"))

        executor1.onCommand(Breakpoint(7))
        executor1.onCommand(ListBreakpoints)
        assertThat(baos1.getString(), `is`("[Breakpoint(line=7, exprStr=1)]\n"))

        assertThat(baos1.getString(), `is`(""))

        executor1.onCommand(Continue)
        assertThat(baos1.getString(), `is`("""|2
                                              |3
                                              |4
                                              |stopped on line 7
                                              |""".trimMargin()))

        executor1.onCommand(Evaluate(VariableCall(Identifier("x"), 1)))
        assertThat(baos1.getString(), `is`("100\n"))

        executor1.onCommand(Continue)
        assertThat(baos1.getString(), `is`("program stopped\n"))

        executor1.onCommand(Continue)
        assertThat(baos1.getString(), `is`("nothing running\n"))

        executor1.onCommand(Stop)
        assertThat(baos1.getString(), `is`("already stopped\n"))

        executor1.onCommand(Run)
        assertThat(baos1.getString(), `is`("nothing to run\n"))

        executor1.onCommand("evaluate 2 + 2 == 4".toReplCommand())
        assertThat(baos1.getString(), `is`("1\n"))
    }

    @Test
    fun conditionTest() {
        val (executor1, baos1) = initExecutor()

        val ast1 = astFromString("""| var x = 0
                                    | while (x < 10) {
                                    |   x = x + 1
                                    | }
                                    | """.trimMargin())

        executor1.onCommand(Load(ast1))
        assertThat(baos1.getString(), `is`("loaded\n"))
        executor1.onCommand(Breakpoint(3, "x > 3"))
        assertThat(baos1.getString(), `is`(""))

        executor1.onCommand(Run)
        assertThat(baos1.getString(), `is`("stopped on line 3\n"))

        executor1.onCommand(Evaluate("x * 10".toExpr()))
        assertThat(baos1.getString(), `is`("40\n"))

        executor1.onCommand(Breakpoint(3, "x > 7"))
        executor1.onCommand(Continue)
        assertThat(baos1.getString(), `is`("stopped on line 3\n"))

        executor1.onCommand(Evaluate("0 - x * 10".toExpr()))
        assertThat(baos1.getString(), `is`("-80\n"))

        executor1.onCommand(Stop)
        assertThat(baos1.getString(), `is`(""))
    }
}
