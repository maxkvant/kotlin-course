package ru.spbau.mit

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import ru.spbau.mit.ast.Block
import ru.spbau.mit.ast.Expression
import ru.spbau.mit.parser.FunLangLexer
import ru.spbau.mit.parser.FunLangParser
import ru.spbau.mit.repl.Executor
import ru.spbau.mit.repl.toReplCommand

fun genAst(charStream: CharStream): Block {
    val lexer = FunLangLexer(charStream)
    val tokens = CommonTokenStream(lexer)
    val parser = FunLangParser(tokens)
    val visitor = FunLangVisitor()
    return visitor.visit(parser.block()) as Block
}

fun String.toExpr(): Expression {
    val lexer = FunLangLexer(CharStreams.fromString(this))
    val tokens = CommonTokenStream(lexer)
    val parser = FunLangParser(tokens)
    val visitor = FunLangVisitor()
    return visitor.visit(parser.expression()) as Expression
}

fun main(args: Array<String>) {
    val executor = Executor(System.out)
    while (true) {
        try {
            val command = readLine()!!.toReplCommand()
            executor.onCommand(command)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
