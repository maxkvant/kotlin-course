package ru.spbau.mit.repl

import org.antlr.v4.runtime.CharStreams
import ru.spbau.mit.Evaluator
import ru.spbau.mit.ast.Block
import ru.spbau.mit.genAst
import ru.spbau.mit.toExpr
import java.io.PrintStream

fun String.toReplCommand(): Command {
    val string = this.trim()
    return when {
        string.startsWith("load ") -> {
            val (_, fileName) = string.split(' ', limit = 2)
            val ast = genAst(CharStreams.fromFileName(fileName))
            Load(ast)
        }

        string.startsWith("breakpoint ") -> {
            val (_, line) = string.split(' ', limit = 2)
            Breakpoint(line.toInt())
        }

        string.startsWith("condition ") -> {
            val (_, line, exprString) = string.split(' ', limit = 3)
            Breakpoint(line.toInt(), exprString)
        }

        string == "list" -> ListBreakpoints

        string.startsWith("remove ") -> {
            val (_, line) = string.split(' ', limit = 2)
            RemoveBreakpoint(line.toInt())
        }

        string == "run" -> Run

        string.startsWith("evaluate ") -> {
            val (_, exprStr) = string.split(' ', limit = 2)
            Evaluate(exprStr.toExpr())
        }

        string == "stop" -> Stop

        string == "continue" -> Continue

        else -> throw IllegalArgumentException("no such command")
    }
}


class Executor(private val printStream: PrintStream) {
    private var state: State? = null
    private var running: Boolean = false

    fun onCommand(command: Command) {
        fun next() {
            state!!.iterator.let {
                if (it.hasNext()) {
                    printStream.println("stopped on line ${it.next()}")
                } else {
                    state = null
                    running = false
                    printStream.println("program stopped")
                }
            }
        }
        try {
            when (command) {
                is Load -> {
                    state = State(command.ast, printStream)
                    running = false
                    printStream.println("loaded")
                }
                is Breakpoint -> {
                    state?.evaluator?.setBreakpoint(command)
                            ?: printStream.println("breakpoint not added, load some file")
                }
                is RemoveBreakpoint -> {
                    state?.evaluator?.removeBreakpoint(command.line)
                }
                is ListBreakpoints -> {
                    val list = state?.evaluator?.listBreakpoints() ?: emptyList()
                    printStream.println(list)
                }
                is Run -> {
                    if (!running && state != null) {
                        next()
                        running = true
                    } else {
                        printStream.println("nothing to run")
                    }
                }
                is Stop -> {
                    if (state == null) {
                        printStream.println("already stopped")
                    } else {
                        state = null
                        running = false
                    }
                }
                is Continue -> {
                    if (!running) {
                        printStream.println("nothing running")
                    } else {
                        next()
                    }
                }
                is Evaluate -> {
                    val exprResult = (state?.evaluator ?: Evaluator(printStream)).evalExpr(command.expr)
                    printStream.println("$exprResult")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace(printStream)
        }
    }

    private class State(ast: Block, printStream: PrintStream) {
        val evaluator: Evaluator = Evaluator(printStream)
        val iterator: Iterator<Int> = evaluator.evalStatementIterator(ast)
    }
}