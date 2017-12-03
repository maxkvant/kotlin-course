package ru.spbau.mit

import ru.spbau.mit.Ast.*
import java.io.PrintStream
import kotlin.coroutines.experimental.RestrictsSuspension

@RestrictsSuspension
class Evaluator(
    functionScope1: Scope<FunctionDef>,
    variableScope1: Scope<Long>,
    private val printStream: PrintStream
) {
    private class State (
        val functionScope: Scope<FunctionDef>,
        val variableScope: Scope<Long>
    ) {
        var returnValue: Long? = null
            get

        fun enterFunc(func: FunctionDef): State {
            val functionScope2 = Scope(functionScope).apply { put(func.name, func) }
            val variableScope2 = Scope(variableScope)
            return State(functionScope2, variableScope2)
        }

        fun getRes(): Long {
            return returnValue ?: 0L
        }
    }

    private var state: State = State(functionScope1, variableScope1)

    private fun runIfNotReturned(f: () -> Unit): Boolean {
        return when (state.returnValue) {
            null -> {
                f(); true
            }
            else -> false
        }
    }

    fun evalExpr(expr: Expression): Long {
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

    private fun eval(binOp: BinaryOp): Long {
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
                    check (rVal != 0L) { "error error on line ${binOp.line}: / by zero" }
                    lVal / rVal
                }
                Operation.Rem -> {
                    check (rVal != 0L) { "error on line ${binOp.line}: % by zero" }
                    lVal % rVal
                }
            }
    }

    private fun eval(funCall: FunctionCall): Long {
        try {
            val args: List<Long> = funCall.args.map(this::evalExpr)
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

    fun evalStatement(statement: Statement) {
        runIfNotReturned {
            when (statement) {
                is Block -> statement.statements.forEach(this::evalStatement)
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

    private fun eval(whileStatement: While) {
        while (isTrue(whileStatement.loopExpr) && (runIfNotReturned { evalStatement(whileStatement.block) }));
    }

    private fun isTrue(expr: Expression): Boolean {
        return evalExpr(expr) != 0L
    }

    private fun eval(ifStatement: If) {
        runIfNotReturned {
            if (isTrue(ifStatement.boolExpr)) {
                evalStatement(ifStatement.blockTrue)
            } else if (ifStatement.blockFalse != null) {
                evalStatement(ifStatement.blockFalse)
            }
        }
    }

    private fun handleException(line: Int, e: Exception): Nothing {
        throw EvaluatorException("error on line $line: ${e.message}", e)
    }
}

class EvaluatorException(override val message: String, override val cause: Throwable) : RuntimeException()

fun evaluate(block: Block, printStream: PrintStream) {
    val funScope = Scope<FunctionDef>(null)
    funScope.put(FunctionDef.printLn.name, FunctionDef.printLn)
    val evaluator = Evaluator(funScope, Scope(null), printStream)
    evaluator.evalStatement(block)
}