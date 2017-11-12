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
        return visitLogicalOrExpression(ctx!!.arifmeticExpression().logicalOrExpression())
    }

    override fun visitLogicalOrExpression(ctx: FunLangParser.LogicalOrExpressionContext?): Expression {
        return when {
            ctx!!.logicalOrExpression() is FunLangParser.LogicalOrExpressionContext -> {
                val l = visitLogicalOrExpression(ctx.logicalOrExpression())
                val r = visitLogicalAndExpression(ctx.logicalAndExpression())
                genBinOp(l, "||", r)
            }
            else -> visitLogicalAndExpression(ctx.logicalAndExpression())
        }
    }

    override fun visitLogicalAndExpression(ctx: FunLangParser.LogicalAndExpressionContext?): Expression {
        return when {
            ctx!!.logicalAndExpression() is FunLangParser.LogicalAndExpressionContext -> {
                val l = visitLogicalAndExpression(ctx.logicalAndExpression())
                val r = visitRelationalExpression(ctx.relationalExpression())
                genBinOp(l, "&&", r)
            }
            else -> visitRelationalExpression(ctx.relationalExpression())
        }
    }

    override fun visitRelationalExpression(ctx: FunLangParser.RelationalExpressionContext?): Expression {
        val expressions = ctx!!.additiveExpression().map(this::visitAdditiveExpression)
        return when (expressions.size) {
            1 -> expressions[0]
            2 -> genBinOp(expressions[0], ctx.relationalOp().text, expressions[1])
            else -> throw RuntimeException()
        }
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
            ctx.expression() is FunLangParser.ExpressionContext ->
                visitExpression(ctx.expression())
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
            ctx!!.multiplicativeExpression() is FunLangParser.MultiplicativeExpressionContext -> {
                val l = visitMultiplicativeExpression(ctx.multiplicativeExpression())
                val op = ctx.multiplicativeOp().text
                val r = visitSimpleExpression(ctx.simpleExpression())
                genBinOp(l, op, r)
            }
            else -> visitSimpleExpression(ctx.simpleExpression())
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
            else -> visitMultiplicativeExpression(ctx.multiplicativeExpression())

        }
    }

    private fun genBinOp(l: Expression, op: String, r: Expression): BinaryOp {
        val op1 = Operation.values().filter { it.str == op }.first()
        return BinaryOp(l, op1, r)
    }
}