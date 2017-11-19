package ru.spbau.maxim

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Test

internal class TexDslTest {
    @Test
    fun testDocument() {
        val tex = document {
            documentClass("beamer", "12pt" to null)
            usepackage("babel", "russian" to null)
        }

        assertThat(tex.toString(), `is`(
                """|\documentclass[12pt]{beamer}
                   |\usepackage[russian]{babel}
                   |\begin{document}
                   |\end{document}
                   |""".trimMargin()))
    }

    @Test
    fun testItems() {
        val items = listOf("a", "b", "ab")
        val tex = document {
            itemize {
                for (el in items) {
                    item { +"- $el" }
                }
            }
        }

        assertThat(tex.toString(), `is`(
                """|\begin{document}
                   |  \begin{itemize}
                   |    \item
                   |      - a
                   |    \item
                   |      - b
                   |    \item
                   |      - ab
                   |  \end{itemize}
                   |\end{document}
                   |""".trimMargin()))

        val tex2 = document {
            enumerate { item { +"hello" } }
        }
        assertThat(tex2.toString(), `is`(
                """|\begin{document}
                   |  \begin{enumerate}
                   |    \item
                   |      hello
                   |  \end{enumerate}
                   |\end{document}
                   |""".trimMargin()))
    }

    @Test
    fun testAlignment() {
        val tex = document {
            left { right { center { } } }
        }

        assertThat(tex.toString(), `is`(
                """|\begin{document}
                   |  \begin{flushleft}
                   |    \begin{flushright}
                   |      \begin{center}
                   |      \end{center}
                   |    \end{flushright}
                   |  \end{flushleft}
                   |\end{document}
                   |""".trimMargin()))
    }

    @Test
    fun testMathMode() {
        val tex = document {
            mathMode {
                +"1 + 2 = 3"
                +"\\log(e) = 1"
            }
        }

        assertThat(tex.toString(), `is`(
                """|\begin{document}
                   |  $$1 + 2 = 3
                   |  \log(e) = 1
                   |  $$
                   |\end{document}
                   |""".trimMargin()))
    }

    @Test
    fun testCustomTag() {
        val tex = document {
            customTag("pyglist", "language" to "kotlin", "language3" to "java") {
                +"""|var c: Int = 10
                    |c += 1
                 """
            }
        }
        assertThat(tex.toString(), `is`(
                """|\begin{document}
                   |  \begin{pyglist}[language=kotlin,language3=java]
                   |    var c: Int = 10
                   |    c += 1
                   |  \end{pyglist}
                   |\end{document}
                   |""".trimMargin()))
    }

    @Test
    fun testFrame() {
        val rows = listOf("z", "y", "x")

        val tex = document {
            frame("Frame Title", "arg1" to "arg2") {
                +"$ 1 = 1 $"
                itemize {
                    for (row in rows) {
                        item { +"$row text" }
                    }
                }
            }
        }

        assertThat(tex.toString(), `is`(
                """|\begin{document}
                   |  \begin{frame}[arg1=arg2]{Frame Title}
                   |    ${'$'} 1 = 1 ${'$'}
                   |    \begin{itemize}
                   |      \item
                   |        z text
                   |      \item
                   |        y text
                   |      \item
                   |        x text
                   |    \end{itemize}
                   |  \end{frame}
                   |\end{document}
                   |""".trimMargin()))
    }
}