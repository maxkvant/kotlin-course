package ru.spbau.mit

import ru.spbau.mit.ast.*
import ru.spbau.mit.repl.Breakpoint
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.coroutines.experimental.SequenceBuilder
import kotlin.coroutines.experimental.buildIterator

class Evaluator(
        functionScope: Scope<FunctionDef>,
        variableScope: Scope<Long>,
        private val printStream: PrintStream
) {
    constructor(printStream: PrintStream) : this(
            Scope<FunctionDef>(null).apply { put(FunctionDef.printLn.name, FunctionDef.printLn) },
            Scope(null),
            printStream
    )

    private var state: State = State(functionScope, variableScope)
    private val breakpoints = mutableMapOf<Int, Breakpoint>()

    fun setBreakpoint(breakpoint: Breakpoint) {
        breakpoints[breakpoint.line] = breakpoint
    }

    fun removeBreakpoint(line: Int) = breakpoints.remove(line)

    fun evalStatementIterator(statement: Statement): Iterator<Int> = buildIterator {
        evalStatement(statement)
    }

    fun evalExpr(expr: Expression): Long {
        var res: Long? = null
        buildIterator { res = evalExpr(expr) }.forEach { }
        return res!!
    }

    fun evalStatement(statement: Statement): Long {
        evalStatementIterator(statement).forEach { }
        return state.getRes()
    }

    fun listBreakpoints() = breakpoints.values.toList()

    private suspend fun SequenceBuilder<Int>.runIfNotReturned(f: suspend SequenceBuilder<Int>.() -> Any?): Boolean {
        return when (state.returnValue) {
            null -> {
                f(); true
            }
            else -> false
        }
    }

    private suspend fun SequenceBuilder<Int>.evalExpr(expr: Expression): Long {
        return when (expr) {
            is Literal -> expr.num
            is BinaryOp -> eval(expr)
            is VariableCall -> try {
                state.variableScope.get(expr.name)
            } catch (e: Exception) {
                handleException(expr.line, e)
            }
            is FunctionCall -> eval(expr)
        }
    }

    private suspend fun SequenceBuilder<Int>.eval(binOp: BinaryOp): Long {
        val lVal = evalExpr(binOp.l)
        val rVal = evalExpr(binOp.r)
        fun toLong(x: Boolean): Long = when (x) {
            true -> 1L
            false -> 0L
        }

        return when (binOp.operation) {
            Operation.Eq -> toLong(lVal == rVal)
            Operation.Neq -> toLong(rVal != rVal)
            Operation.Or -> toLong((lVal != 0L) || (rVal != 0L))
            Operation.And -> toLong((lVal != 0L) || (rVal != 0L))
            Operation.Lt -> toLong(lVal < rVal)
            Operation.Le -> toLong(lVal <= rVal)
            Operation.Gt -> toLong(lVal > rVal)
            Operation.Ge -> toLong(lVal >= rVal)
            Operation.Plus -> lVal + rVal
            Operation.Minus -> lVal - rVal
            Operation.Multiply -> lVal * rVal
            Operation.Divide -> {
                check(rVal != 0L) { "error on line ${binOp.line}: / by zero" }
                lVal / rVal
            }
            Operation.Rem -> {
                check(rVal != 0L) { "error on line ${binOp.line}: % by zero" }
                lVal % rVal
            }
        }
    }

    private suspend fun SequenceBuilder<Int>.eval(funCall: FunctionCall): Long {
        try {
            val args: List<Long> = funCall.args.map { evalExpr(it) }
            val func = state.functionScope.get(funCall.name)


            if (func == FunctionDef.printLn) {
                printStream.println(args.joinToString(", "))
                return 0
            } else {
                val oldState = state

                try {
                    check(func.params.size == args.size) { "error on line ${funCall.line} wrong number of params" }

                    state = state.enterFunc(func)

                    func.params.zip(args).forEach({ (name, value) ->
                        state.variableScope.put(name, value)
                    })

                    evalStatement(func.block)
                } finally {
                    val res = state.getRes()
                    state = oldState
                    return res
                }
            }
        } catch (e: Exception) {
            handleException(funCall.line, e)
        }
    }

    private suspend fun SequenceBuilder<Int>.onLine(line: Int) {
        val condition = breakpoints[line]
        if (condition != null && isTrueExpr(condition.expr)) {
            yield(line)
        }
    }

    private suspend fun SequenceBuilder<Int>.evalStatement(statement: Statement) {
        runIfNotReturned {
            onLine(statement.line)
            when (statement) {
                is Block -> for (statement1 in statement.statements) {
                    evalStatement(statement1)
                }
                is Return -> state.returnValue = evalExpr(statement.expr)
                is VariableDef -> {
                    state.variableScope.put(statement.name, statement.value?.let { evalExpr(it) } ?: 0L)
                }
                is FunctionDef -> state.functionScope.put(statement.name, statement)
                is Assignment -> state.variableScope.set(statement.identifier, evalExpr(statement.expr))
                is If -> eval(statement)
                is While -> eval(statement)
                is Expression -> evalExpr(statement)
            }
        }
    }

    private suspend fun SequenceBuilder<Int>.eval(whileStatement: While) {
        while (isTrue(whileStatement.loopExpr) && (runIfNotReturned { evalStatement(whileStatement.block) }));
    }

    private suspend fun SequenceBuilder<Int>.isTrue(expr: Expression): Boolean {
        return evalExpr(expr) != 0L
    }

    private suspend fun SequenceBuilder<Int>.eval(ifStatement: If) {
        runIfNotReturned {
            if (isTrue(ifStatement.boolExpr)) {
                evalStatement(ifStatement.blockTrue)
            } else if (ifStatement.blockFalse != null) {
                evalStatement(ifStatement.blockFalse)
            }
        }
    }

    @Suppress("unused")
    private suspend fun SequenceBuilder<Int>.handleException(line: Int, e: Exception): Nothing {
        throw EvaluatorException("error on line $line: ${e.message}", e)
    }

    private fun isTrueExpr(expr: Expression): Boolean {
        return evalExpr(expr) != 0L
    }

    private class State(
            val functionScope: Scope<FunctionDef>,
            val variableScope: Scope<Long>
    ) {
        var returnValue: Long? = null

        fun enterFunc(func: FunctionDef): State {
            val functionScope2 = Scope(functionScope).apply { put(func.name, func) }
            val variableScope2 = Scope(variableScope)
            return State(functionScope2, variableScope2)
        }

        fun getRes(): Long {
            return returnValue ?: 0L
        }
    }
}

class EvaluatorException(override val message: String, override val cause: Throwable) : RuntimeException()

fun evaluateExpr(expr: Expression): Long {
    return Evaluator(PrintStream(ByteArrayOutputStream())).evalExpr(expr)
}

fun evaluate(block: Block, printStream: PrintStream) {
    Evaluator(printStream).evalStatement(block)
}