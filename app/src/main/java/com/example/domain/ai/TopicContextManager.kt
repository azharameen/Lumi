package com.example.domain.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class TopicStatus {
    ACTIVE,
    SUSPENDED,
    COMPLETED
}

/**
 * Encapsulates a distinct contextual discussion thread / topic.
 * Allows switching away to transactional tasks without losing conversational state.
 */
data class TopicFrame(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val domainSkill: String,
    val summary: String = "",
    val turnCount: Int = 1,
    val status: TopicStatus = TopicStatus.ACTIVE,
    val keyEntities: List<String> = emptyList(),
    val suggestedFollowUps: List<String> = emptyList(),
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

/**
 * Enterprise Topic Context Stack & Resumption Manager.
 * Solves:
 * 1. Long context switching (e.g. London trip -> wellness -> GitHub)
 * 2. Topic suspension and retrieval ("Back to the trip", "What was I planning earlier?")
 * 3. Topic-aware dynamic prompt suggestions
 * 4. Topic summarization and recording to long-term memory
 */
class TopicContextManager private constructor() {

    private val topicFrames = ConcurrentHashMap<String, TopicFrame>()
    private val _activeTopicId = MutableStateFlow<String?>(null)
    val activeTopicId: StateFlow<String?> = _activeTopicId.asStateFlow()

    companion object {
        @Volatile
        private var instance: TopicContextManager? = null

        fun getInstance(): TopicContextManager {
            return instance ?: synchronized(this) {
                instance ?: TopicContextManager().also { instance = it }
            }
        }
    }

    /**
     * Retrieves the current active or most recently suspended topic frame.
     */
    fun getActiveTopic(): TopicFrame? {
        val activeId = _activeTopicId.value ?: return getMostRecentSuspendedTopic()
        return topicFrames[activeId]
    }

    fun getMostRecentSuspendedTopic(): TopicFrame? {
        return topicFrames.values
            .filter { it.status == TopicStatus.SUSPENDED }
            .maxByOrNull { it.lastActiveTimestamp }
    }

    /**
     * Records or updates conversational topic flow based on user message and active skill.
     */
    fun trackTopicTurn(userMessage: String, skillName: String, isTransactional: Boolean) {
        if (isTransactional) {
            // Suspend the active conversational topic when a transactional command arrives
            _activeTopicId.value?.let { currentId ->
                topicFrames[currentId]?.let { currentFrame ->
                    if (currentFrame.status == TopicStatus.ACTIVE) {
                        topicFrames[currentId] = currentFrame.copy(
                            status = TopicStatus.SUSPENDED,
                            lastActiveTimestamp = System.currentTimeMillis()
                        )
                    }
                }
            }
            return
        }

        // Check for explicit topic resumption intent (e.g. "back to the trip", "as we were saying")
        val isResumption = isResumptionIntent(userMessage)
        if (isResumption) {
            val suspended = getMostRecentSuspendedTopic()
            if (suspended != null) {
                val resumed = suspended.copy(
                    status = TopicStatus.ACTIVE,
                    lastActiveTimestamp = System.currentTimeMillis()
                )
                topicFrames[resumed.id] = resumed
                _activeTopicId.value = resumed.id
                return
            }
        }

        // Conversational skill (e.g. LIFE_ORGANIZER, WELLNESS, GENERAL_COMPANION)
        val currentId = _activeTopicId.value
        val existingFrame = if (currentId != null) topicFrames[currentId] else null

        if (existingFrame != null && existingFrame.status == TopicStatus.ACTIVE && existingFrame.domainSkill == skillName) {
            // Continue existing topic
            topicFrames[existingFrame.id] = existingFrame.copy(
                turnCount = existingFrame.turnCount + 1,
                lastActiveTimestamp = System.currentTimeMillis()
            )
        } else {
            // New distinct topic started
            val newTitle = extractTopicTitle(userMessage, skillName)
            val followUps = generateFollowUpsForTopic(newTitle, skillName)
            val newFrame = TopicFrame(
                title = newTitle,
                domainSkill = skillName,
                suggestedFollowUps = followUps,
                status = TopicStatus.ACTIVE
            )
            topicFrames[newFrame.id] = newFrame
            _activeTopicId.value = newFrame.id
        }
    }

    /**
     * Updates the summary and entities of a topic frame.
     */
    fun updateTopicSummary(topicId: String, summary: String, entities: List<String> = emptyList()) {
        topicFrames[topicId]?.let { frame ->
            topicFrames[topicId] = frame.copy(
                summary = summary,
                keyEntities = if (entities.isNotEmpty()) entities else frame.keyEntities
            )
        }
    }

    /**
     * Checks if the message explicitly signals returning to an earlier topic.
     */
    private fun isResumptionIntent(message: String): Boolean {
        val lower = message.lowercase(java.util.Locale.ROOT)
        return lower.contains("back to") ||
               lower.contains("continue with") ||
               lower.contains("resume") ||
               lower.contains("as we were saying") ||
               lower.contains("earlier we were") ||
               lower.contains("what about the")
    }

    private fun extractTopicTitle(query: String, skillName: String): String {
        val cleaned = query.take(45).replace(Regex("""[^\w\s\-]"""), "").trim()
        return when {
            cleaned.isNotBlank() -> cleaned
            skillName == "LIFE_ORGANIZER" -> "Daily Planning & Agenda"
            skillName == "WELLNESS" -> "Mindfulness & Health"
            else -> "Companion Chat"
        }
    }

    private fun generateFollowUpsForTopic(topicTitle: String, skillName: String): List<String> {
        val lower = topicTitle.lowercase(java.util.Locale.ROOT)
        return when {
            lower.contains("trip") || lower.contains("travel") || lower.contains("vacation") -> listOf(
                "🗓️ 3-day itinerary",
                "🎒 Packing checklist",
                "🏨 Hotel and stay options",
                "💰 Budget breakdown"
            )
            lower.contains("workout") || lower.contains("gym") || lower.contains("exercise") -> listOf(
                "🏋️ Push-pull split",
                "💧 Hydration & fuel",
                "⏱️ Rest intervals",
                "📈 Log PR weights"
            )
            lower.contains("study") || lower.contains("exam") || lower.contains("read") -> listOf(
                "🍅 25-minute Pomodoro block",
                "📝 Key summary flashcards",
                "🎯 Practice quiz questions",
                "🧠 Active recall recap"
            )
            skillName == "WELLNESS" -> listOf(
                "🌿 2-minute breath reset",
                "💧 Log water intake",
                "💭 Evening gratitude journal",
                "🌙 Sleep wind-down checklist"
            )
            else -> listOf(
                "✨ Break this into 3 action steps",
                "📅 Add to my daily schedule",
                "💡 Brainstorm 3 alternatives",
                "📝 Summarize key decisions"
            )
        }
    }
}
