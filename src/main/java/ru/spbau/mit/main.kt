package ru.spbau.mit

import java.io.IOException

data class Dancer(val point: Point, val t: Int) {
    init {
        require((point.x == 0) != (point.y == 0))
    }

    val potential: Int = point.x + point.y - t
    val vertical: Boolean = point.x == 0
}

data class Point(val x: Int, val y: Int, val pos: Int)

fun solve(w: Int, h: Int, dancers: List<Dancer>): List<Point> {
    val groupsByPotential: Map<Int, List<Dancer>> = dancers.groupBy { it.potential }

    return groupsByPotential.values.flatMap { group ->
        val curDancers: List<Dancer> = group
                .sortedWith(compareBy<Dancer> { it.point.x }.thenByDescending { it.point.y })

        val resPoints = curDancers.map { dancer ->
            if (dancer.vertical) Point(w, dancer.point.y, dancer.point.pos) else Point(dancer.point.x, h, dancer.point.pos)
        }.sortedWith(compareBy<Point> { it.x }.thenByDescending { it.y })

        curDancers.zip(resPoints, { dancer, point -> Point(point.x, point.y, dancer.point.pos) })
    }.sortedBy { it.pos }
}

//http://codeforces.com/contest/848/submission/32551207
fun main(args: Array<String>) {
    val (n: Int, w: Int, h: Int) = (readLine() ?: throw IOException("wrong input"))
            .split(" ")
            .map(String::toInt)

    val dancers: List<Dancer> = List(n, { i ->
        val (g: Int, p: Int, t: Int) = (readLine() ?: throw IOException("wrong input"))
                .split(" ")
                .map(String::toInt)

        val (x, y) = when (g) {
            1 -> Pair(p, 0)
            2 -> Pair(0, p)
            else -> throw RuntimeException("g=${g} isn't in {1, 2}")
        }
        Dancer(Point(x, y, i), t)
    })

    val res: List<Point> = solve(w, h, dancers)
    res.forEach { point -> println("${point.x} ${point.y}") }
}