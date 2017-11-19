package ru.spbau.maxim

sealed class Element

enum class CommandType { LIKE_TAG, BEGIN_END }

typealias Arg = Pair<String, String?>
typealias Args = List<Arg>

sealed class AbstractCommand(val args: Args, val name: String) : Element() {
    abstract fun children(): List<Element>
    abstract val commandType: CommandType
}

sealed class Command(args: Args, name: String) : AbstractCommand(args, name) {
    private val body = mutableListOf<Element>()

    override fun children() = body

    protected fun <T : Element> addElement(t: T, init: T.() -> Unit): T {
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

    operator fun String.unaryPlus() {
        body.add(TextElement(this))
    }
}

class TextElement(val text: String) : Element()

class Frame(val title: String, args: Args) : Command(args, "frame") {
    override val commandType: CommandType = CommandType.BEGIN_END
}

class MathMode : Element() {
    val text = StringBuilder()
        get

    operator fun String.unaryPlus() {
        text.append(this + "\n")
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
}

class CustomTag(name: String, args: Args) : Command(args, name) {
    override val commandType = CommandType.LIKE_TAG
}

class Enumerate : Items("enumerate")
class Itemize : Items("itemize")

class Item(args: Args) : Command(args, "item") {
    override val commandType = CommandType.LIKE_TAG
}

class DocumentClass(val documentClass: String, args: Args) : AbstractCommand(args, "documentclass") {
    override val commandType = CommandType.LIKE_TAG
    override fun children(): List<Element> = emptyList()
}

class UsePackage(val packageName: String) : AbstractCommand(emptyList(), "usepackage") {
    override val commandType = CommandType.LIKE_TAG

    override fun children(): List<Element> = emptyList()
}

class Document(args: Args) : Command(args, "document") {
    override val commandType = CommandType.BEGIN_END

    internal val header = mutableListOf<AbstractCommand>()
        get

    fun usepackage(vararg newPackages: String) {
        newPackages.forEach { header.add(UsePackage(it)) }
    }

    fun documentClass(documentClass: String, vararg args: Arg) {
        header.add(DocumentClass(documentClass, args.toList()))
    }
}

fun document(vararg args: Arg, init: Document.() -> Unit): Document = Document(args.toList()).also(init)