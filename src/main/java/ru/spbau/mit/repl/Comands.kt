package ru.spbau.mit.repl

import ru.spbau.mit.ast.Block
import ru.spbau.mit.ast.Expression
import ru.spbau.mit.toExpr

sealed class Command

data class Breakpoint(val line: Int, val exprStr: String) : Command() {
    constructor(line: Int) : this(line, "1")

    val expr: Expression = exprStr.toExpr()
}

data class RemoveBreakpoint(val line: Int) : Command()

object ListBreakpoints : Command()

object Run : Command()

data class Load(val ast: Block) : Command()

data class Evaluate(val expr: Expression) : Command()

object Stop : Command()

object Continue : Command()
