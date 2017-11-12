package ru.spbau.mit

import ru.spbau.mit.Ast.*
import ru.spbau.mit.parser.FunLangBaseVisitor
import ru.spbau.mit.parser.FunLangParser

class Visitor : FunLangBaseVisitor<Node>() {
    override fun visitBlock(ctx: FunLangParser.BlockContext?): Block {
        return Block(ctx!!.statement()
                .map(this::visitStatement))
    }

    override fun visitStatement(ctx: FunLangParser.StatementContext?): Statement {
        ctx!!
        return when {
            ctx.assignment() is FunLangParser.AssignmentContext ->
                Assignment(visitIdentifier(ctx.assignment().identifier()),
                        visitExpression(ctx.assignment().expression()))
            ctx.expression() is FunLangParser.ExpressionContext ->
                visitExpression(ctx.expression())
            ctx.function() is FunLangParser.FunctionContext ->
                visitFunction(ctx.function())
            ctx.variable() is FunLangParser.VariableContext ->
                visitVariable(ctx.variable())
            ctx.iff() is FunLangParser.IffContext ->
                visitIff(ctx.iff())
            ctx.whilee() is FunLangParser.WhileeContext ->
                visitWhilee(ctx.whilee())
            ctx.returnn() is FunLangParser.ReturnnContext ->
                visitReturnn(ctx.returnn())
            else -> throw RuntimeException()
        }
    }

    override fun visitWhilee(ctx: FunLangParser.WhileeContext?): While {
        return While(visitExpression(ctx!!.expression()),
                visitBlockWithBraces(ctx.blockWithBraces()))
    }

    override fun visitReturnn(ctx: FunLangParser.ReturnnContext?): Return {
        return Return(visitExpression(ctx!!.expression()))
    }


    override fun visitVariable(ctx: FunLangParser.VariableContext?): VariableDef {
        return VariableDef(visitIdentifier(ctx!!.identifier()),
                visitExpression(ctx.expression()))
    }


    override fun visitFunction(ctx: FunLangParser.FunctionContext?): FunctionDef {
        val params = ctx!!.parameterNames().identifier().map { Identifier(it.text) }
        val name = visitIdentifier(ctx.identifier())
        val block = visitBlockWithBraces(ctx.blockWithBraces())
        return FunctionDef(name, params, block)
    }

    override fun visitIdentifier(ctx: FunLangParser.IdentifierContext?): Identifier {
        return Identifier(ctx!!.text)
    }

    override fun visitBlockWithBraces(ctx: FunLangParser.BlockWithBracesContext?): Block {
        return visitBlock(ctx!!.block())
    }

    override fun visitExpression(ctx: FunLangParser.ExpressionContext?): Expression {
        return when {
            ctx!!.simpleExpression() is FunLangParser.SimpleExpressionContext ->
                visitSimpleExpression(ctx.simpleExpression())
            ctx.binaryExpression() is FunLangParser.BinaryExpressionContext ->
                visitBinaryExpression(ctx.binaryExpression())
            else -> throw RuntimeException()
        }
    }

    override fun visitBinaryExpression(ctx: FunLangParser.BinaryExpressionContext?): Expression {
        return visitAdditiveExpression(ctx!!.additiveExpression())
    }

    override fun visitAssignment(ctx: FunLangParser.AssignmentContext?): Node {
        return Assignment(visitIdentifier(ctx!!.identifier()),
                visitExpression(ctx.expression()))
    }

    override fun visitSimpleExpression(ctx: FunLangParser.SimpleExpressionContext?): Expression {
        return when {
            ctx!!.identifier() is FunLangParser.IdentifierContext ->
                VariableCall(visitIdentifier(ctx.identifier()))
            ctx.literal() is FunLangParser.LiteralContext ->
                Literal(ctx.literal().text.toLong())
            ctx.simpleExpression() is FunLangParser.SimpleExpressionContext ->
                visitSimpleExpression(ctx.simpleExpression())
            ctx.functionCall() is FunLangParser.FunctionCallContext -> {
                val expressions = ctx.functionCall().arguments().expression().map {
                    visitExpression(it)
                }
                FunctionCall(visitIdentifier(ctx.functionCall().identifier()), expressions)
            }
            else -> throw RuntimeException()
        }
    }

    override fun visitIff(ctx: FunLangParser.IffContext?): If {
        val blocks: List<Block> = ctx!!.blockWithBraces()
                .map(this::visitBlockWithBraces)

        val expression = visitExpression(ctx.expression())
        return when (blocks.size) {
            1 -> If(expression, blocks[0], Block(emptyList<Statement>()))
            2 -> If(expression, blocks[0], blocks[1])
            else -> throw RuntimeException()
        }
    }

    override fun visitMultiplicativeExpression(ctx: FunLangParser.MultiplicativeExpressionContext?): Expression {
        return when {
            ctx!!.simpleExpression() is FunLangParser.SimpleExpressionContext ->
                visitSimpleExpression(ctx.simpleExpression())
            ctx.multiplicativeExpression() is FunLangParser.SimpleExpressionContext -> {
                val l = visitMultiplicativeExpression(ctx.multiplicativeExpression())
                val op = ctx.multiplicativeOp().text
                val r = visitExpression(ctx.expression())
                genBinOp(l, op, r)
            }
            else -> throw RuntimeException()
        }
    }

    override fun visitAdditiveExpression(ctx: FunLangParser.AdditiveExpressionContext?): Expression {
        return when {
            ctx!!.additiveExpression() is FunLangParser.AdditiveExpressionContext -> {
                val l = visitAdditiveExpression(ctx.additiveExpression())
                val r = visitMultiplicativeExpression(ctx.multiplicativeExpression())
                val op = ctx.additiveOp().text
                genBinOp(l, op, r)
            }
            ctx.multiplicativeExpression() is FunLangParser.MultiplicativeExpressionContext ->
                visitMultiplicativeExpression(ctx.multiplicativeExpression())
            else -> throw RuntimeException()
        }
    }

    private fun genBinOp(l: Expression, op: String, r: Expression): BinaryOp {
        val op1 = Operation.values().filter { it.str == op }.first()
        return BinaryOp(l, op1, r)
    }

    /*
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
            Operation.Divide -> lVal / rVal
            Operation.Rem -> lVal % rVal
        }
     */
}