package ru.spbau.mit

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import ru.spbau.mit.Ast.Block
import ru.spbau.mit.parser.FunLangLexer
import ru.spbau.mit.parser.FunLangParser

fun evaluateStr(str: String) {
    val input = ANTLRInputStream(str)
    val lexer = FunLangLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = FunLangParser(tokens)
    val visitor = Visitor()
    val ast = visitor.visit(parser.block()) as Block
    evaluate(ast)
}

fun main(args: Array<String>) {
    evaluateStr("""| var a = 0
                   | fun f(a) {
                   |     return a + 1
                   | }
                   | while (a < 100) {
                   |   a = f(a)
                   |   var b = 0
                   |   println(a, b)
                   | }
                   |
                 """.trimMargin())


    evaluateStr("""| fun gcd(a, b) {
                   |     if (b) {
                   |         return gcd(b, a % b)
                   |     } else {
                   |         return a
                   |     }
                   | }
                   |
                   | println(gcd(2, 3))
                   |
                   | println(gcd(24, 16))
                   |
                   """.trimMargin())


}
