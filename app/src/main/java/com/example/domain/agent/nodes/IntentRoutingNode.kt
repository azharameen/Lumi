package com.example.domain.agent.nodes

import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiInlineData
import com.example.data.remote.GeminiPart
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.util.Base64

class IntentRoutingNode : AgentNode {
    override val name: String = "INTENT_ROUTING"

    override suspend fun execute(state: AgentState): AgentState {
        val queryLower = state.userQuery.lowercase()

        // Local intent heuristic (determine if cloud or local edge execution is preferred)
        val isLocal = queryLower.contains("battery") ||
                      queryLower.contains("time") ||
                      queryLower.contains("offline")

        val skill = when {
            queryLower.contains("email") || queryLower.contains("sheet") || queryLower.contains("doc") || 
            queryLower.contains("slide") || queryLower.contains("keep") || queryLower.contains("drive") || 
            queryLower.contains("presentation") -> "GOOGLE_WORKSPACE"

            queryLower.contains("github") || queryLower.contains("issue") || queryLower.contains("repo") || 
            queryLower.contains("commit") || queryLower.contains("pull request") || queryLower.contains("pr") -> "GITHUB"

            queryLower.contains("slack") || queryLower.contains("channel") || queryLower.contains("dnd") || 
            queryLower.contains("status") -> "SLACK"

            queryLower.contains("task") || queryLower.contains("todo") || queryLower.contains("schedule") || 
            queryLower.contains("event") || queryLower.contains("calendar") || queryLower.contains("remind") || 
            queryLower.contains("routine") -> "LIFE_ORGANIZER"

            queryLower.contains("breathe") || queryLower.contains("wellness") || queryLower.contains("mood") || 
            queryLower.contains("stress") || queryLower.contains("hydrate") || queryLower.contains("gratitude") || 
            queryLower.contains("health") || queryLower.contains("calm") -> "WELLNESS"

            else -> "GENERAL_COMPANION"
        }

        val contentsList = mutableListOf<GeminiContent>()

        // Add history turns (last 6 turns)
        for (turn in state.history.takeLast(6)) {
            val role = if (turn.first == "USER") "user" else "model"
            contentsList.add(
                GeminiContent(
                    role = role,
                    parts = listOf(GeminiPart(text = turn.second))
                )
            )
        }

        // Add current user turn
        val currentParts = mutableListOf<GeminiPart>()
        currentParts.add(GeminiPart(text = state.userQuery))
        if (state.imageAttachment != null) {
            currentParts.add(
                GeminiPart(
                    inlineData = GeminiInlineData(
                        mimeType = "image/jpeg",
                        data = state.imageAttachment.toBase64()
                    )
                )
            )
        }

        contentsList.add(
            GeminiContent(
                role = "user",
                parts = currentParts
            )
        )

        return state.copy(
            isLocalExecution = isLocal,
            selectedSkillName = skill,
            contentsList = contentsList
        )
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
