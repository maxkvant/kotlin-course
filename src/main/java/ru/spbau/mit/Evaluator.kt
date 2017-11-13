package ru.spbau.mit

import ru.spbau.mit.Ast.*
import java.io.PrintStream

class Evaluator(val functionScope: Scope<FunctionDef>,
                val variableScope: Scope<Long>,
                val printStream: PrintStream) {
    private var returnValue: Long? = null

    fun getRes(): Long {
        return returnValue ?: 0L
    }

    private fun tryRun(f: () -> Unit): Boolean {
        return when (returnValue) {
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
                variableScope.get(expr.name)
            } catch (e: Throwable) {
                throw EvaluatorException("error on line ${expr.line}: ${e.message}")
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

        return try {
            when (binOp.operation) {
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
                Operation.Divide -> lVal / rVal
                Operation.Rem -> lVal % rVal
            }
        } catch (e: Throwable) {
            throw EvaluatorException("arithmetic error on line ${binOp.line}: ${e.message}")
        }
    }

    private fun eval(funCall: FunctionCall): Long {
        try {
            val args: List<Long> = funCall.args.map(this::evalExpr)
            val func = functionScope.get(funCall.name)


            if (func == Ast.printLn) {
                printStream.println(args.joinToString(", "))
                return 0
            } else {
                val variableScope2 = Scope(variableScope)
                val functionScope2 = Scope(functionScope)
                functionScope2.put(func.name, func)

                func.params.zip(args).forEach({ (name, value) ->
                    variableScope2.put(name, value)
                })
                val evaluator = Evaluator(functionScope2, variableScope2, printStream)
                evaluator.evalStatement(func.block)
                return evaluator.getRes()
            }
        } catch (e: Throwable) {
            throw EvaluatorException("error on line ${funCall.line}: ${e.message}")
        }
    }

    fun evalStatement(statement: Statement) {
        tryRun {
            when (statement) {
                is Block -> statement.statements.forEach(this::evalStatement)
                is Return -> returnValue = evalExpr(statement.expr)
                is VariableDef -> {
                    variableScope.put(statement.name, statement.value?.let { evalExpr(it) } ?: 0L)
                }
                is FunctionDef -> functionScope.put(statement.name, statement)
                is Assignment -> variableScope.set(statement.identifier, evalExpr(statement.expr))
                is If -> eval(statement)
                is While -> eval(statement)
                is Expression -> evalExpr(statement)
            }
        }
    }

    private fun eval(whileStatement: While) {
        while (isTrue(whileStatement.loopExpr) && (tryRun { evalStatement(whileStatement.block) }));
    }

    private fun isTrue(expr: Expression): Boolean {
        return evalExpr(expr) != 0L
    }

    private fun eval(ifStatement: If) {
        tryRun {
            if (isTrue(ifStatement.boolExpr)) {
                evalStatement(ifStatement.blockTrue)
            } else {
                evalStatement(ifStatement.blockFalse)
            }
        }
    }
}

class EvaluatorException(override val message: String) : RuntimeException()

fun evaluate(block: Block, printStream: PrintStream) {
    val funScope = Scope<FunctionDef>(null)
    funScope.put(Ast.printLn.name, Ast.printLn)
    val evaluator = Evaluator(funScope, Scope(null), printStream)
    evaluator.evalStatement(block)
    evaluator.getRes()
}