package ru.spbau.maxim

fun main(args: Array<String>) {
    val rows = listOf<String>("a", "b", "c", "d")
    val tex = (document {
        documentClass("beamer")
        usepackage("babel", "russian" /* varargs */)
        frame(title = "frametitle", args = "arg1" to "arg2") {
            itemize {
                item {
                    for (row in rows) {
                        item { +"$row text" }
                    }
                }
            }

            mathMode {
                +" a + b = 10 "
            }

            // begin{pyglist}[language=kotlin]...\end{pyglist}
            customTag(name = "pyglist", args = "language" to "kotlin") {
                +"""
               |val a = 1
               |
               |val b = 3
               |
               |var c 10
            """
            }

            enumerate {
                item {
                    for (row in rows) {
                        item { +"$row text" }
                    }
                }
            }
        }
    })
    Writer(System.out).printElement(tex)
}