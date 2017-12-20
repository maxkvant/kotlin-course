package ru.spbau.maxim

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

@DslMarker
annotation class TexElementMarker

@TexElementMarker
sealed class Element {
    internal abstract fun print(writerState: WriterState)
}

enum class CommandType { LIKE_TAG, BEGIN_END }

typealias Arg = Pair<String, String?>
typealias Args = List<Arg>

sealed class AbstractCommand(private val args: Args, private val name: String) : Element() {
    abstract fun children(): List<Element>
    abstract val commandType: CommandType

    override fun print(writerState: WriterState) {
        writerState.apply {
            fun printChildren(children: List<Element>) {
                ident {
                    children.forEach { it.print(this) }
                }
            }

            when (commandType) {
                CommandType.BEGIN_END -> {
                    println("\\begin{$name}" + argsToString(args) + additionalInfo())
                    printChildren(children())
                    println("\\end{$name}")
                }
                CommandType.LIKE_TAG -> {
                    println("\\$name" + argsToString(args) + additionalInfo())
                    printChildren(children())
                }
            }
        }
    }

    internal abstract fun additionalInfo(): String
}

sealed class Command(args: Args, name: String) : AbstractCommand(args, name) {
    private val body = mutableListOf<Element>()

    override fun children() = body.toList()

    private fun <T : Element> addElement(t: T, init: T.() -> Unit): T {
        t.init()
        return t.also { body.add(t) }
    }

    fun frame(title: String, vararg args: Arg, init: Frame.() -> Unit): Frame
            = addElement(Frame(title, args.toList()), init)

    fun enumerate(init: Items.() -> Unit): Enumerate = addElement(Enumerate(), init)

    fun itemize(init: Itemize.() -> Unit): Itemize = addElement(Itemize(), init)

    fun mathMode(init: MathMode.() -> Unit) = addElement(MathMode(), init)

    fun customTag(name: String, vararg args: Arg, init: CustomTag.() -> Unit)
            = addElement(CustomTag(name, args.toList()), init)

    fun center(init: Center.() -> Unit) = addElement(Center(), init)

    fun left(init: Left.() -> Unit) = addElement(Left(), init)

    fun right(init: Right.() -> Unit) = addElement(Right(), init)

    operator fun String.unaryPlus() {
        body.add(TextElement(this))
    }

    override fun additionalInfo(): String = ""
}

class TextElement(private val text: String) : Element() {
    override fun print(writerState: WriterState) {
        writerState.apply { println(text.trim()) }
    }
}

class Frame(private val title: String, args: Args) : Command(args, "frame") {
    override val commandType: CommandType = CommandType.BEGIN_END
    override fun additionalInfo(): String = "{$title}"
}

class Left : Command(emptyList(), "flushleft") {
    override val commandType = CommandType.BEGIN_END
}

class Right : Command(emptyList(), "flushright") {
    override val commandType = CommandType.BEGIN_END
}

class Center : Command(emptyList(), "center") {
    override val commandType = CommandType.BEGIN_END
}

class MathMode : Element() {
    private val text = StringBuilder()
    private fun text() = text.toString()

    operator fun String.unaryPlus() {
        text.append(this + "\n")
    }

    override fun print(writerState: WriterState) {
        writerState.apply { println("$$${text()}$$".trim()) }
    }
}

open class Items(name: String) : AbstractCommand(emptyList(), name) {
    override val commandType = CommandType.BEGIN_END

    private val items = mutableListOf<Item>()
    override fun children() = items

    fun item(vararg arg: Arg, init: Item.() -> Unit): Item {
        val item = Item(arg.toList())
        item.init()
        items.add(item)
        return item
    }

    override fun additionalInfo(): String = ""
}

class CustomTag(name: String, args: Args) : Command(args, name) {
    override val commandType = CommandType.BEGIN_END
}

class Enumerate : Items("enumerate")
class Itemize : Items("itemize")

class Item(args: Args) : Command(args, "item") {
    override val commandType = CommandType.LIKE_TAG
}

class DocumentClass(private val documentClass: String, args: Args) : AbstractCommand(args, "documentclass") {
    override val commandType = CommandType.LIKE_TAG
    override fun children(): List<Element> = emptyList()
    override fun additionalInfo(): String = "{$documentClass}"
}

class UsePackage(args: Args, private val packageName: String) : AbstractCommand(args, "usepackage") {
    override val commandType = CommandType.LIKE_TAG
    override fun children(): List<Element> = emptyList()
    override fun additionalInfo(): String = "{$packageName}"
}

class Document : Command(emptyList(), "document") {
    override val commandType = CommandType.BEGIN_END
    private val header = mutableListOf<AbstractCommand>()

    private fun header() = header.toList()

    fun usepackage(newPackage: String, vararg args: Arg) {
        header.add(UsePackage(args.toList(), newPackage))
    }

    fun documentClass(documentClass: String, vararg args: Arg) {
        header.add(DocumentClass(documentClass, args.toList()))
    }

    private fun printToStream(outputStream: OutputStream) {
        val printStream = PrintStream(outputStream)
        print(WriterState(printStream))
    }

    override fun toString(): String {
        val baos = ByteArrayOutputStream()
        printToStream(baos)
        val res = String(baos.toByteArray())
        baos.close()
        return res
    }

    override fun print(writerState: WriterState) {
        header().forEach { it.print(writerState) }
        super.print(writerState)
    }
}

fun document(init: Document.() -> Unit): Document = Document().also(init)