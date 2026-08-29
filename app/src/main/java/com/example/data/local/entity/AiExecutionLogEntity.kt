package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_execution_logs")
data class AiExecutionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskCategory: String, // e.g. "Companion Chat", "Vision & Screen", "Wellness Reflection", "Task / Schedule Tool", "Mindful Breathing"
    val engineType: String, // "ON_DEVICE_GEMMA", "CLOUD_GEMINI", "LOCAL_RULE_ENGINE"
    val modelName: String, // "gemma-2b-it-int4", "gemini-2.5-flash", "lumi-local-v1"
    val promptPreview: String,
    val responsePreview: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val estimatedCostUsd: Double,
    val startTimeMillis: Long,
    val finishTimeMillis: Long,
    val durationMs: Long,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val hardwareTarget: String = "GPU (OpenCL/Vulkan)", // e.g. "GPU", "CPU", "Google Tensor Cloud"
    val routingReason: String = "",
    val fallbackTriggered: Boolean = false
)
