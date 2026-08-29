package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String, // USER, LUMI, SYSTEM
    val content: String,
    val petEmotion: String = "HAPPY", // HAPPY, CALM, ENERGETIC, SLEEPY, THINKING, LOVING, PLAYFUL
    val toolUsedName: String? = null,
    val toolResultJson: String? = null,
    val imageBase64OrUri: String? = null
)
