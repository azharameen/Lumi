package com.example.domain.memory

import com.example.domain.ai.BpeTokenizer
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 1. Working Memory Tier:
 * In-memory active turn conversation buffer with strict token budgeting and turn limits.
 */
class WorkingMemory(
    val maxTurns: Int = 10,
    val maxTokens: Int = 2048
) {
    private val buffer = ConcurrentLinkedQueue<Pair<String, String>>()
    var activeScratchpad: String = ""

    fun addTurn(speaker: String, text: String) {
        buffer.add(speaker to text)
        pruneToBudget()
    }

    private fun pruneToBudget() {
        // 1. Maintain turn limit
        while (buffer.size > maxTurns) {
            buffer.poll()
        }

        // 2. Maintain strict token budget using BpeTokenizer
        while (calculateTotalTokens() > maxTokens && buffer.isNotEmpty()) {
            buffer.poll()
        }
    }

    fun calculateTotalTokens(): Int {
        var total = 0
        for (turn in buffer) {
            total += BpeTokenizer.countTokens(turn.first) + BpeTokenizer.countTokens(turn.second)
        }
        if (activeScratchpad.isNotBlank()) {
            total += BpeTokenizer.countTokens(activeScratchpad)
        }
        return total
    }

    fun getTurns(): List<Pair<String, String>> = buffer.toList()

    fun clear() {
        buffer.clear()
        activeScratchpad = ""
    }
}

