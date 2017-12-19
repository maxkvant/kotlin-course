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

    internal fun printCommand(command: AbstractCommand) {
        fun printChildren(children: List<Element>) {
            ident++
            children.forEach { it.print(this) }
            ident--
        }

        when (command.commandType) {
            CommandType.BEGIN_END -> {
                println("\\begin{" + command.name + "}" + argsToString(command.args) + command.additionalInfo())
                printChildren(command.children())
                println("\\end{" + command.name + "}")
            }
            CommandType.LIKE_TAG -> {
                println("\\" + command.name + "" + argsToString(command.args) + command.additionalInfo())
                printChildren(command.children())
            }
        }
    }

    private fun argsToString(args: Args): String {
        return when (args.size) {
            0 -> ""
            else -> args.joinToString(",", "[", "]", transform = this::argToString)
        }
    }
}