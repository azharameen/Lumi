package com.example.domain.prompt

import com.example.data.local.entity.ChatMessageEntity
import com.example.data.remote.FirebaseAiCloudEngine
import com.example.data.remote.OnDeviceGemmaEngine
import com.example.domain.tools.ToolCategory
import com.example.domain.tools.ToolRegistry
import java.util.Calendar

/**
 * Generates dynamic prompt suggestions and template categories
 * intelligently powered by On-Device Local LLM (or Cloud Gemini fallback)
 * based on conversation context and recent turns — completely eliminating keyword heuristics.
 */
object DynamicPromptSuggester {

    /**
     * Fast non-blocking starter prompts for initial render before LLM inference completes.
     */
    fun getInitialPrompts(
        recentMessages: List<ChatMessageEntity> = emptyList(),
        tipOfTheDay: String? = null
    ): List<String> {
        val prompts = mutableListOf<String>()

        // 0. Topic Resumption Pill: 1-tap resumption if a previous topic was suspended
        val suspendedTopic = com.example.domain.ai.TopicContextManager.getInstance().getMostRecentSuspendedTopic()
        if (suspendedTopic != null && suspendedTopic.title.isNotBlank()) {
            prompts.add("🔙 Back to ${suspendedTopic.title.take(22)}")
        }

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> {
                prompts.add("✨ Plan morning focus blocks")
                prompts.add("🎯 Prioritize top daily quests")
            }
            in 12..16 -> {
                prompts.add("🌿 Quick 2-minute breath reset")
                prompts.add("⚡ Afternoon energy check-in")
            }
            in 17..21 -> {
                prompts.add("📝 Review today's completed goals")
                prompts.add("💧 Log hydration & evening mood")
            }
            else -> {
                prompts.add("🌙 Wind-down reflection")
                prompts.add("💭 Tomorrow's high-level preview")
            }
        }

        val effectiveTip = tipOfTheDay ?: try {
            org.koin.core.context.GlobalContext.getOrNull()?.getOrNull<com.example.data.firebase.LumiRemoteConfigManager>()?.config?.value?.companionTipOfTheDay
        } catch (_: Exception) { null }

        if (!effectiveTip.isNullOrBlank()) {
            val shortTip = if (effectiveTip.length > 36) effectiveTip.take(33) + "..." else effectiveTip
            prompts.add("💡 $shortTip")
        }

        return prompts.distinct().take(5)
    }

    /**
     * Intelligently generates dynamic quick prompts for the horizontal chip bar
     * using the On-Device Local LLM (or Cloud Gemini) to understand the recent dialogue context.
     * Zero keyword matching.
     */
    suspend fun getQuickPrompts(
        recentMessages: List<ChatMessageEntity> = emptyList(),
        onDeviceGemmaEngine: OnDeviceGemmaEngine? = null,
        tipOfTheDay: String? = null
    ): List<String> {
        val prompts = mutableListOf<String>()

        // 0. Topic Resumption Pill: If a previous topic was suspended, offer 1-tap resumption
        val suspendedTopic = com.example.domain.ai.TopicContextManager.getInstance().getMostRecentSuspendedTopic()
        if (suspendedTopic != null && suspendedTopic.title.isNotBlank()) {
            prompts.add("🔙 Back to ${suspendedTopic.title.take(22)}")
        }

        // 1. LLM-Driven Dynamic Suggestion Generation from Conversation Dialogue
        if (recentMessages.isNotEmpty()) {
            val dialogueTurns = recentMessages.takeLast(4).map { msg ->
                Pair(msg.sender, msg.content)
            }

            val aiSuggestions = try {
                if (onDeviceGemmaEngine?.isModelReady() == true) {
                    onDeviceGemmaEngine.generateFollowUpSuggestions(dialogueTurns, maxSuggestions = 4)
                } else {
                    FirebaseAiCloudEngine.getInstance().generateFollowUpSuggestions(dialogueTurns, maxSuggestions = 4)
                }
            } catch (_: Exception) {
                emptyList()
            }

            prompts.addAll(aiSuggestions)
        }

        // 2. If list needs starters (brand new chat or offline fallback without models)
        if (prompts.size < 3) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            when (hour) {
                in 5..11 -> {
                    prompts.add("✨ Plan morning focus blocks")
                    prompts.add("🎯 Prioritize top daily quests")
                }
                in 12..16 -> {
                    prompts.add("🌿 Quick 2-minute breath reset")
                    prompts.add("⚡ Afternoon energy check-in")
                }
                in 17..21 -> {
                    prompts.add("📝 Review today's completed goals")
                    prompts.add("💧 Log hydration & evening mood")
                }
                else -> {
                    prompts.add("🌙 Wind-down reflection")
                    prompts.add("💭 Tomorrow's high-level preview")
                }
            }
        }

        // 3. Remote Config Tip of the day if space allows
        val effectiveTip = tipOfTheDay ?: try {
            org.koin.core.context.GlobalContext.getOrNull()?.getOrNull<com.example.data.firebase.LumiRemoteConfigManager>()?.config?.value?.companionTipOfTheDay
        } catch (_: Exception) { null }

        if (!effectiveTip.isNullOrBlank() && prompts.size < 5) {
            val shortTip = if (effectiveTip.length > 36) effectiveTip.take(33) + "..." else effectiveTip
            prompts.add("💡 $shortTip")
        }

        return prompts.distinct().take(6)
    }

    /**
     * Dynamically generates categorized prompt templates by querying registered tools,
     * current context, and contextual domains in Lumi.
     */
    fun getTemplateCategories(recentMessages: List<ChatMessageEntity> = emptyList()): List<Pair<String, List<String>>> {
        val categories = mutableListOf<Pair<String, List<String>>>()

        // 0. Suspended Topic Resumption
        val suspended = com.example.domain.ai.TopicContextManager.getInstance().getMostRecentSuspendedTopic()
        if (suspended != null) {
            categories.add(
                "🔙 Resume Discussion" to listOf(
                    "Back to ${suspended.title}",
                    "What were we discussing earlier about ${suspended.title}?",
                    "Summarize where we left off with ${suspended.title}"
                )
            )
        }

        // 1. Contextual Productivity & Planning
        categories.add(
            "🚀 Focus & Planning" to listOf(
                "Plan my day efficiently with focused time blocks",
                "Break down my complex goal into 3 actionable quests",
                "Help me prioritize: Task A vs Task B",
                "Summarize upcoming calendar agenda and deadlines"
            )
        )

        // 2. Mindfulness, Health & Vitality
        categories.add(
            "🌿 Mindfulness & Vitality" to listOf(
                "Guide me through a calming 4-7-8 breathing session",
                "Log water intake and energetic mood score",
                "I am feeling overwhelmed. Help me ground myself",
                "Review my sleep, hydration, and habit balance this week"
            )
        )

        // 3. Dynamically build category from Registered Tools in ToolRegistry
        val allTools = ToolRegistry.getInstance().getAllTools()
        if (allTools.isNotEmpty()) {
            val toolPrompts = allTools.map { tool ->
                "Run ${tool.displayName}: ${tool.description.take(50)}"
            }.take(8)

            if (toolPrompts.isNotEmpty()) {
                categories.add("🛠️ Available Device & System Actions" to toolPrompts)
            }
        }

        // 4. Creative Brainstorming & Reflection
        categories.add(
            "💭 Reflection & Synthesis" to listOf(
                "Write an uplifting gratitude reflection for this morning",
                "Brainstorm 5 innovative ideas for my project",
                "Synthesize key takeaways from our recent discussion",
                "What new skill should I focus on learning next?"
            )
        )

        return categories
    }
}
