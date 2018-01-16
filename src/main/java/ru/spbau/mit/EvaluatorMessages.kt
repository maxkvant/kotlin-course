package ru.spbau.mit

import kotlin.coroutines.experimental.Continuation

data class EvaluatorMessage(val line: Int, val continuation: Continuation<Unit>)

interface EvaluatorMessagesReceiver {
    fun onMessage(message: EvaluatorMessage)
}