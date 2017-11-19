package ru.spbau.maxim

import java.io.PrintStream

class Writer(private val printWriter: PrintStream) {
    private var ident: Int = 0

    private fun prefix() = " ".repeat(ident * 2)

    private fun println(string: String) {
        printWriter.println(prefix() + string)
    }


    private fun String.trim(): String {
        return this.trimMargin().replace("\n", "\n" + prefix())
    }

    fun printElement(element: Element) {
        when (element) {
            is AbstractCommand -> printCommand(element)
            is TextElement -> println(element.text.trim())
            is MathMode -> println("$$" + (element.text() + "$$").trim())
        }
    }

    private fun printCommand(command: AbstractCommand) {
        (command as? Document)?.header()?.forEach(this::printCommand)
        val additionalInfo = when (command) {
            is DocumentClass -> "{" + command.documentClass + "}"
            is UsePackage -> "{" + command.packageName + "}"
            is Frame -> "{" + command.title + "}"
            else -> ""
        }

        fun printChildren(children: List<Element>) {
            ident++
            children.forEach(this::printElement)
            ident--
        }

        when (command.commandType) {
            CommandType.BEGIN_END -> {
                println("\\begin{" + command.name + "}" + argsToString(command.args) + additionalInfo)
                printChildren(command.children())
                println("\\end{" + command.name + "}")
            }
            CommandType.LIKE_TAG -> {
                println("\\" + command.name + "" + argsToString(command.args) + additionalInfo)
                printChildren(command.children())
            }
        }
    }

    private fun argToString(arg: Arg): String {
        val (arg1, arg2) = arg
        return arg1 + when (arg2) {
            is String -> "=" + arg2
            else -> ""
        }
    }

    private fun argsToString(args: Args): String {
        return when (args.size) {
            0 -> ""
            else -> args.joinToString(",", "[", "]", transform = this::argToString)
        }
    }
}