package ru.spbau.mit

interface DebugAction {
    suspend fun onLine(line: Int)
}