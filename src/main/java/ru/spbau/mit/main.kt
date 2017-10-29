package ru.spbau.mit

data class Dancer(val x0: Int, val y0: Int, val t: Int, val pos: Int) {
    init {
        require((x0 == 0) != (y0 == 0))
    }

    val potential: Int = x0 + y0 - t
    val vertical: Boolean = x0 == 0
}

data class Point(val x: Int, val y: Int, val pos: Int)

fun solve(w: Int, h: Int, dancers: List<Dancer>): List<Point> {
    val groupsByPotential: Map<Int, List<Dancer>> = dancers.groupBy { dancer -> dancer.potential }

    return groupsByPotential.values.flatMap { group ->
        val curDancers: List<Dancer> = group
                .sortedWith(compareBy<Dancer> { dancer -> dancer.x0 }.thenByDescending { dancer -> dancer.y0 })

        val resPoints = curDancers.map { dancer ->
            when (dancer.vertical) {
                true -> Point(w, dancer.y0, dancer.pos)
                false -> Point(dancer.x0, h, dancer.pos)
            }
        }.sortedWith(compareBy<Point> { p -> p.x }.thenByDescending { p -> p.y })

        curDancers.zip(resPoints, { dancer, point -> Point(point.x, point.y, dancer.pos) })
    }.sortedBy { point -> point.pos }
}

//http://codeforces.com/contest/848/submission/31870682
fun main(args: Array<String>) {
    val (n: Int, w: Int, h: Int) = readLine()!!.split(" ").map(String::toInt)
    val dancers: List<Dancer> = (0 until n).map { i ->
        val (g: Int, p: Int, t: Int) = readLine()!!.split(" ").map(String::toInt)
        val (x, y) = when (g) {
            1 -> Pair(p, 0)
            2 -> Pair(0, p)
            else -> throw RuntimeException("g=${g} isn't in {1, 2}")
        }
        Dancer(x, y, t, i)
    }.toList()
    val res: List<Point> = solve(w, h, dancers)
    res.forEach { point -> println("${point.x} ${point.y}") }
}