package ru.spbau.mit

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import ru.spbau.mit.Ast.Block
import ru.spbau.mit.parser.FunLangLexer
import ru.spbau.mit.parser.FunLangParser

fun main(args: Array<String>) {
    val str = """| var a = 0
                 | while (a - 100000000) {
                 |   a = a + 1
                 |   var b = 0
                 | }
                 |
              """.trimMargin()

    val input = ANTLRInputStream(str)
    val lexer = FunLangLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = FunLangParser(tokens)
    val visitor = Visitor()
    val ast = visitor.visit(parser.block()) as Block
    evaluate(ast)
}
