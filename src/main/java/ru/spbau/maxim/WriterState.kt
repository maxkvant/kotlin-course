package ru.spbau.maxim

import java.io.PrintStream

internal class WriterState(private val printWriter: PrintStream) {
    private var ident: Int = 0

    private fun prefix() = " ".repeat(ident * 2)

    internal fun String.trim(): String {
        return this.trimMargin().replace("\n", "\n" + prefix())
    }

    internal fun println(string: String) {
        printWriter.println(prefix() + string)
    }

    private fun argToString(arg: Arg): String {
        val (arg1, arg2) = arg
        return arg1 + when (arg2) {
            is String -> "=" + arg2
            else -> ""
        }
    }

    internal fun ident(init: WriterState.() -> Unit) {
        ident++
        this.init()
        ident--
    }

    internal fun argsToString(args: Args): String {
        return when (args.size) {
            0 -> ""
            else -> args.joinToString(",", "[", "]", transform = this::argToString)
        }
    }
}