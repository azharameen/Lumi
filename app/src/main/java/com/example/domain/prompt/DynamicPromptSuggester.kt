package com.example.domain.prompt

import com.example.domain.tools.ToolCategory
import com.example.domain.tools.ToolRegistry
import java.util.Calendar

/**
 * Generates dynamic prompt suggestions and template categories
 * based on contextual state: current time of day, active registered tools,
 * and remote configuration tips — eliminating hardcoded static lists.
 */
object DynamicPromptSuggester {

    /**
     * Generates dynamic quick prompts for the horizontal chip bar.
     * Incorporates time of day context, registered tools, and optional tip of the day.
     */
    fun getQuickPrompts(tipOfTheDay: String? = null): List<String> {
        val prompts = mutableListOf<String>()

        // 0. Active Topic Contextual Follow-up Chips (Pillar 4)
        val activeTopic = com.example.domain.ai.TopicContextManager.getInstance().getActiveTopic()
        if (activeTopic != null && activeTopic.suggestedFollowUps.isNotEmpty()) {
            prompts.addAll(activeTopic.suggestedFollowUps)
            if (activeTopic.status == com.example.domain.ai.TopicStatus.SUSPENDED) {
                prompts.add("🔙 Back to ${activeTopic.title.take(20)}")
            }
        }

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // 1. Time-of-day contextual starter
        when (hour) {
            in 5..11 -> {
                prompts.add("✨ Plan my morning focus blocks")
                prompts.add("🎯 Prioritize top 3 daily quests")
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

        // 2. Incorporate Remote Config tip if available
        if (!effectiveTip.isNullOrBlank()) {
            val shortTip = if (effectiveTip.length > 36) effectiveTip.take(33) + "..." else effectiveTip
            prompts.add("💡 $shortTip")
        }

        // 3. Dynamically sample from registered tools in ToolRegistry
        val tools = ToolRegistry.getInstance().getAllTools()
        val healthTool = tools.firstOrNull { it.category == ToolCategory.HEALTH }
        if (healthTool != null) {
            prompts.add("❤️ ${healthTool.displayName}")
        }
        val calendarTool = tools.firstOrNull { it.category == ToolCategory.CALENDAR }
        if (calendarTool != null) {
            prompts.add("📅 ${calendarTool.displayName}")
        }
        val systemTool = tools.firstOrNull { it.category == ToolCategory.SYSTEM }
        if (systemTool != null) {
            prompts.add("⚙️ ${systemTool.displayName}")
        }

        return prompts.distinct()
    }

    /**
     * Dynamically generates categorized prompt templates by querying registered tools
     * and contextual domains in Lumi.
     */
    fun getTemplateCategories(): List<Pair<String, List<String>>> {
        val categories = mutableListOf<Pair<String, List<String>>>()

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
