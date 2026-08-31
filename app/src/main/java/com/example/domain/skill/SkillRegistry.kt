package com.example.domain.skill

import com.example.data.remote.GeminiToolWrapper
import com.example.domain.skill.impl.*

object SkillRegistry {

    private val skills: Map<String, AgentSkill> = listOf(
        LifeOrganizerSkill(),
        GoogleWorkspaceSkill(),
        GithubSkill(),
        SlackSkill(),
        WellnessSkill(),
        GeneralCompanionSkill()
    ).associateBy { it.id }

    fun getSkill(id: String?): AgentSkill {
        return skills[id] ?: skills["GENERAL_COMPANION"]!!
    }

    fun getToolsForSkill(id: String?): List<GeminiToolWrapper> {
        return getSkill(id).tools
    }

    fun getAllSkills(): List<AgentSkill> {
        return skills.values.toList()
    }
}
