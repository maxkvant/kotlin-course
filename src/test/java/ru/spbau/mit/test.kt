package ru.spbau.mit

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Test

class TestSource {
    @Test
    fun test1() {
        val w = 10
        val h = 8
        val dancers = listOf(
                Dancer(1, 0, 10, 0),
                Dancer(4, 0, 13, 1),
                Dancer(7, 0, 1, 2),
                Dancer(8, 0, 2, 3),
                Dancer(0, 2, 0, 4),
                Dancer(0, 5, 14, 5),
                Dancer(0, 6, 0, 6),
                Dancer(0, 6, 1, 7)
        )

        assertThat(solve(w, h, dancers), `is`(listOf(
                Point(4, 8, 0),
                Point(10, 5, 1),
                Point(8, 8, 2),
                Point(10, 6, 3),
                Point(10, 2, 4),
                Point(1, 8, 5),
                Point(7, 8, 6),
                Point(10, 6, 7)
        )))
    }

    @Test
    fun test2() {
        val w = 2
        val h = 3
        val dancers = listOf(
                Dancer(1, 0, 2, 0),
                Dancer(0, 1, 1, 1),
                Dancer(1, 0, 5, 2)
        )

        assertThat(solve(w, h, dancers), `is`(listOf(
                Point(1, 3, 0),
                Point(2, 1, 1),
                Point(1, 3, 2)
        )))
    }
}
