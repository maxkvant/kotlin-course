package ru.spbau.mit

import ru.spbau.mit.Ast.*
import ru.spbau.mit.parser.FunLangBaseVisitor
import ru.spbau.mit.parser.FunLangParser

class FunLangVisitor : FunLangBaseVisitor<Node>() {
    override fun visitBlock(ctx: FunLangParser.BlockContext): Block {
        return Block(ctx.statement()
                .map(this::visitStatement),
                ctx.start.line)
    }

    override fun visitStatement(ctx: FunLangParser.StatementContext): Statement {
        val line = ctx.start.line
        return when {
            ctx.assignment() != null ->
                Assignment(visitIdentifier(ctx.assignment().identifier()),
                        visitExpression(ctx.assignment().expression()),
                        line)
            ctx.expression() != null ->
                visitExpression(ctx.expression())
            ctx.function() != null ->
                visitFunction(ctx.function())
            ctx.variable() != null ->
                visitVariable(ctx.variable())
            ctx.ifStatement() != null ->
                visitIfStatement(ctx.ifStatement())
            ctx.whileStatement() != null ->
                visitWhileStatement(ctx.whileStatement())
            ctx.returnStatement() != null ->
                visitReturnStatement(ctx.returnStatement())
            else -> throw RuntimeException()
        }
    }

    override fun visitWhileStatement(ctx: FunLangParser.WhileStatementContext): While {
        return While(visitExpression(ctx.expression()),
                visitBlockWithBraces(ctx.blockWithBraces()),
                ctx.start.line)
    }

    override fun visitReturnStatement(ctx: FunLangParser.ReturnStatementContext): Return {
        return Return(visitExpression(ctx.expression()), ctx.start.line)
    }


    override fun visitVariable(ctx: FunLangParser.VariableContext): VariableDef {
        return VariableDef(visitIdentifier(ctx.identifier()),
                visitExpression(ctx.expression()),
                ctx.start.line)
    }


    override fun visitFunction(ctx: FunLangParser.FunctionContext): FunctionDef {
        val params = ctx.parameterNames().identifier().map { Identifier(it.text) }
        val name = visitIdentifier(ctx.identifier())
        val block = visitBlockWithBraces(ctx.blockWithBraces())
        return FunctionDef(name, params, block, ctx.start.line)
    }

    override fun visitIdentifier(ctx: FunLangParser.IdentifierContext): Identifier {
        return Identifier(ctx.text)
    }

    override fun visitBlockWithBraces(ctx: FunLangParser.BlockWithBracesContext): Block {
        return Block(ctx.block().statement().map(this::visitStatement),
                     ctx.start.line)
    }

    override fun visitExpression(ctx: FunLangParser.ExpressionContext): Expression {
        return visitLogicalOrExpression(ctx.arifmeticExpression().logicalOrExpression())
    }

    override fun visitLogicalOrExpression(ctx: FunLangParser.LogicalOrExpressionContext): Expression {
        return when {
            ctx.logicalOrExpression() != null -> {
                val l = visitLogicalOrExpression(ctx.logicalOrExpression())
                val r = visitLogicalAndExpression(ctx.logicalAndExpression())
                genBinOp(l, "||", r, ctx.start.line)
            }
            else -> visitLogicalAndExpression(ctx.logicalAndExpression())
        }
    }

    override fun visitLogicalAndExpression(ctx: FunLangParser.LogicalAndExpressionContext): Expression {
        return when {
            ctx.logicalAndExpression() != null -> {
                val l = visitLogicalAndExpression(ctx.logicalAndExpression())
                val r = visitRelationalExpression(ctx.relationalExpression())
                genBinOp(l, "&&", r, ctx.start.line)
            }
            else -> visitRelationalExpression(ctx.relationalExpression())
        }
    }

    override fun visitRelationalExpression(ctx: FunLangParser.RelationalExpressionContext): Expression {
        val expressions = ctx.additiveExpression().map(this::visitAdditiveExpression)
        return when (expressions.size) {
            1 -> expressions[0]
            2 -> genBinOp(expressions[0], ctx.relationalOp().text, expressions[1], ctx.start.line)
            else -> throw RuntimeException()
        }
    }


    override fun visitAssignment(ctx: FunLangParser.AssignmentContext): Node {
        return Assignment(visitIdentifier(ctx.identifier()),
                visitExpression(ctx.expression()),
                ctx.start.line)
    }

    override fun visitSimpleExpression(ctx: FunLangParser.SimpleExpressionContext): Expression {
        val line = ctx.start.line
        return when {
            ctx.identifier() != null ->
                VariableCall(visitIdentifier(ctx.identifier()), line)
            ctx.literal() != null ->
                Literal(ctx.literal().text.toLong(), line)
            ctx.expression() != null ->
                visitExpression(ctx.expression())
            ctx.functionCall() != null -> {
                val expressions = ctx.functionCall().arguments().expression().map {
                    visitExpression(it)
                }
                FunctionCall(visitIdentifier(ctx.functionCall().identifier()), expressions, line)
            }
            else -> throw RuntimeException()
        }
    }

    override fun visitIfStatement(ctx: FunLangParser.IfStatementContext): If {
        val blocks: List<Block> = ctx.blockWithBraces()
                .map(this::visitBlockWithBraces)

        val expression = visitExpression(ctx.expression())
        val line = ctx.start.line

        return when (blocks.size) {
            1 -> If(expression, blocks[0], null, line)
            2 -> If(expression, blocks[0], blocks[1], line)
            else -> throw RuntimeException()
        }
    }

    override fun visitMultiplicativeExpression(ctx: FunLangParser.MultiplicativeExpressionContext): Expression {
        return when {
            ctx.multiplicativeExpression() != null -> {
                val l = visitMultiplicativeExpression(ctx.multiplicativeExpression())
                val op = ctx.multiplicativeOp().text
                val r = visitSimpleExpression(ctx.simpleExpression())
                genBinOp(l, op, r, ctx.start.line)
            }
            else -> visitSimpleExpression(ctx.simpleExpression())
        }
    }

    override fun visitAdditiveExpression(ctx: FunLangParser.AdditiveExpressionContext): Expression {
        return when {
            ctx.additiveExpression() != null -> {
                val l = visitAdditiveExpression(ctx.additiveExpression())
                val r = visitMultiplicativeExpression(ctx.multiplicativeExpression())
                val op = ctx.additiveOp().text
                genBinOp(l, op, r, ctx.start.line)
            }
            else -> visitMultiplicativeExpression(ctx.multiplicativeExpression())
        }
    }

    private fun genBinOp(l: Expression, op: String, r: Expression, line: Int): BinaryOp {
        val op1 = Operation.values().first { it.str == op }
        return BinaryOp(l, op1, r, line)
    }
}