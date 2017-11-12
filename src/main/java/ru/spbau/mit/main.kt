package ru.spbau.mit

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import ru.spbau.mit.Ast.Block
import ru.spbau.mit.parser.FunLangLexer
import ru.spbau.mit.parser.FunLangParser

fun main(args: Array<String>) {
    val str = """| var a = 0
                 | fun f(a) {
                 |     return a + 1
                 | }
                 | while (a < 100) {
                 |   a = f(a)
                 |   var b = 0
                 |   println(a, b)
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
