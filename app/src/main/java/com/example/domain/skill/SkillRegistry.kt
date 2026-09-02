package com.example.domain.skill

import com.example.data.remote.GeminiToolWrapper
import com.example.domain.skill.impl.*
import java.util.concurrent.ConcurrentHashMap

class SkillRegistry {

    companion object {
        @Volatile
        private var INSTANCE: SkillRegistry? = null

        fun getInstance(): SkillRegistry {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SkillRegistry().also { INSTANCE = it }
            }
        }
    }

    private val skills = ConcurrentHashMap<String, AgentSkill>()

    init {
        // Register default skills
        registerSkill(LifeOrganizerSkill())
        registerSkill(GoogleWorkspaceSkill())
        registerSkill(GithubSkill())
        registerSkill(SlackSkill())
        registerSkill(WellnessSkill())
        registerSkill(GeneralCompanionSkill())
    }

    fun registerSkill(skill: AgentSkill) {
        skills[skill.id] = skill
    }

    fun getSkill(id: String?): AgentSkill {
        return skills[id] ?: skills["GENERAL_COMPANION"] ?: GeneralCompanionSkill()
    }

    fun getToolsForSkill(id: String?): List<GeminiToolWrapper> {
        return getSkill(id).tools
    }

    fun getAllSkills(): List<AgentSkill> {
        return skills.values.toList()
    }
}
