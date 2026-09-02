package com.example.domain.memory

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 1. Working Memory Tier:
 * In-memory active turn conversation buffer (4-6 turns) and agent scratchpad context.
 */
class WorkingMemory(private val maxTurns: Int = 6) {
    private val buffer = ConcurrentLinkedQueue<Pair<String, String>>()
    var activeScratchpad: String = ""

    fun addTurn(speaker: String, text: String) {
        buffer.add(speaker to text)
        while (buffer.size > maxTurns) {
            buffer.poll()
        }
    }

    fun getTurns(): List<Pair<String, String>> = buffer.toList()

    fun clear() {
        buffer.clear()
        activeScratchpad = ""
    }
}
