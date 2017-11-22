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
                Dancer(Point(1, 0, 0), 10),
                Dancer(Point(4, 0, 1), 13),
                Dancer(Point(7, 0, 2), 1),
                Dancer(Point(8, 0, 3), 2),
                Dancer(Point(0, 2, 4), 0),
                Dancer(Point(0, 5, 5), 14),
                Dancer(Point(0, 6, 6), 0),
                Dancer(Point(0, 6, 7), 1)
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
                Dancer(Point(1, 0, 0), 2),
                Dancer(Point(0, 1, 1), 1),
                Dancer(Point(1, 0, 2), 5)
        )

        assertThat(solve(w, h, dancers), `is`(listOf(
                Point(1, 3, 0),
                Point(2, 1, 1),
                Point(1, 3, 2)
        )))
    }
}
