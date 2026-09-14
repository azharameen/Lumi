package com.example.domain.memory

import com.example.domain.ai.BpeTokenizer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Represents a conversation turn in working memory with optional pinning.
 */
data class WorkingMemoryTurn(
    val speaker: String,
    val text: String,
    val isPinned: Boolean = false,
    val isToolCall: Boolean = false,
    val isToolResult: Boolean = false,
    val toolCallId: String? = null
)

/**
 * 1. Working Memory Tier:
 * In-memory active turn conversation buffer with strict token budgeting, turn limits,
 * and pinned system instruction/context preservation.
 */
class WorkingMemory(
    val maxTurns: Int = 10,
    val maxTokens: Int = 2048
) {
    private val buffer = ConcurrentLinkedQueue<WorkingMemoryTurn>()
    private val mutex = Mutex()
    var activeScratchpad: String = ""

    fun addTurn(
        speaker: String,
        text: String,
        isPinned: Boolean = false,
        isToolCall: Boolean = false,
        isToolResult: Boolean = false,
        toolCallId: String? = null
    ) = runBlocking {
        mutex.withLock {
            val inferredIsToolCall = isToolCall || isToolCallSpeaker(speaker, text)
            val inferredIsToolResult = isToolResult || isToolResultSpeaker(speaker, text)

            buffer.add(
                WorkingMemoryTurn(
                    speaker = speaker,
                    text = text,
                    isPinned = isPinned,
                    isToolCall = inferredIsToolCall,
                    isToolResult = inferredIsToolResult,
                    toolCallId = toolCallId
                )
            )
            pruneToBudgetLocked()
        }
    }

    private fun pruneToBudgetLocked() {
        // 1. Maintain turn limit by pruning oldest non-pinned turns atomically with paired tool turns
        while (buffer.size > maxTurns) {
            val pruned = pruneOneUnitLocked()
            if (!pruned) break
        }

        // 2. Maintain strict token budget using BpeTokenizer by pruning oldest non-pinned turns
        while (calculateTotalTokensLocked() > maxTokens && buffer.isNotEmpty()) {
            val pruned = pruneOneUnitLocked()
            if (!pruned) break
        }
    }

    private fun pruneOneUnitLocked(): Boolean {
        val list = buffer.toList()
        for (candidate in list) {
            if (candidate.isPinned) continue

            val paired = findPairedTurn(candidate, list)
            if (paired != null && paired.isPinned) {
                // If paired turn is pinned, skip candidate to avoid leaving orphan or evicting pinned turn
                continue
            }

            // Remove candidate and its paired turn atomically
            buffer.remove(candidate)
            if (paired != null) {
                buffer.remove(paired)
            }
            return true
        }
        return false
    }

    private fun findPairedTurn(turn: WorkingMemoryTurn, list: List<WorkingMemoryTurn>): WorkingMemoryTurn? {
        if (turn.toolCallId != null) {
            return list.find { it != turn && it.toolCallId == turn.toolCallId }
        }

        val index = list.indexOf(turn)
        if (index == -1) return null

        if (turn.isToolCall) {
            // Find paired tool result following this tool call
            if (index + 1 < list.size && list[index + 1].isToolResult) {
                return list[index + 1]
            }
            for (i in index + 1 until list.size) {
                if (list[i].isToolResult) return list[i]
                if (list[i].isToolCall) break
            }
        } else if (turn.isToolResult) {
            // Find paired tool call preceding this tool result
            if (index - 1 >= 0 && list[index - 1].isToolCall) {
                return list[index - 1]
            }
            for (i in index - 1 downTo 0) {
                if (list[i].isToolCall) return list[i]
                if (list[i].isToolResult) break
            }
        }
        return null
    }

    private fun isToolCallSpeaker(speaker: String, text: String): Boolean {
        val s = speaker.lowercase(Locale.ROOT)
        return s.contains("tool_call") || s.contains("function_call") || text.startsWith("Tool Call:") || text.startsWith("[Tool Call]")
    }

    private fun isToolResultSpeaker(speaker: String, text: String): Boolean {
        val s = speaker.lowercase(Locale.ROOT)
        return s.contains("tool_result") || s.contains("tool_observation") || s == "tool" || s == "observation" || text.startsWith("Tool Result:") || text.startsWith("[Tool Result]") || text.startsWith("Observation:")
    }

    fun calculateTotalTokens(): Int = runBlocking {
        mutex.withLock { calculateTotalTokensLocked() }
    }

    private fun calculateTotalTokensLocked(): Int {
        var total = 0
        for (turn in buffer) {
            total += BpeTokenizer.countTokens(turn.speaker) + BpeTokenizer.countTokens(turn.text)
        }
        if (activeScratchpad.isNotBlank()) {
            total += BpeTokenizer.countTokens(activeScratchpad)
        }
        return total
    }

    /**
     * Backward-compatible helper returning List<Pair<speaker, text>>
     */
    fun getTurns(): List<Pair<String, String>> = buffer.map { it.speaker to it.text }

    /**
     * Complete list of turns with pinning metadata
     */
    fun getFullTurns(): List<WorkingMemoryTurn> = buffer.toList()

    fun clear() = runBlocking {
        mutex.withLock {
            buffer.clear()
            activeScratchpad = ""
        }
    }
}
