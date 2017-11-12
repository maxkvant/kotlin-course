package ru.spbau.mit

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import ru.spbau.mit.Ast.Block
import ru.spbau.mit.parser.FunLangLexer
import ru.spbau.mit.parser.FunLangParser

fun genAst(charStream: CharStream): Block {
    val lexer = FunLangLexer(charStream)
    val tokens = CommonTokenStream(lexer)
    val parser = FunLangParser(tokens)
    val visitor = FunLangVisitor()
    val ast = visitor.visit(parser.block()) as Block
    return ast
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        print("Should be 1 argument: FileName")
        return
    }

    val ast = genAst(CharStreams.fromFileName(args[0]))
    CharStreams.fromString("str")
    evaluate(ast, System.out)
}
