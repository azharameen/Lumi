package com.example.data.remote


import android.util.Log
import android.graphics.BitmapFactory
import com.example.data.firebase.LumiAnalyticsManager
import com.example.data.firebase.LumiCrashlyticsManager
import com.example.data.firebase.LumiPerformanceManager
import com.example.data.firebase.LumiRemoteConfigManager
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Enterprise Firebase AI Cloud LLM Engine.
 * 
 * Powered directly by Firebase AI Logic (Gemini 2.5 Flash) with zero client-side API key requirements.
 * Secured and attested at the transport level by Firebase App Check and Google Play Integrity.
 */
class FirebaseAiCloudEngine {

    companion object {
        private const val TAG = "FirebaseAiCloudEngine"
        const val DEFAULT_MODEL = "gemini-2.5-flash"

        @Volatile
        private var instance: FirebaseAiCloudEngine? = null

        fun getInstance(): FirebaseAiCloudEngine {
            return instance ?: synchronized(this) {
                instance ?: FirebaseAiCloudEngine().also { instance = it }
            }
        }
    }

    private val modelCache = ConcurrentHashMap<String, GenerativeModel>()

    private val remoteConfigManager by lazy {
        try {
            GlobalContext.get().get<LumiRemoteConfigManager>()
        } catch (_: Exception) {
            null
        }
    }

    private val performanceManager by lazy {
        try {
            GlobalContext.get().get<LumiPerformanceManager>()
        } catch (_: Exception) {
            null
        }
    }

    private val crashlyticsManager by lazy {
        try {
            GlobalContext.get().get<LumiCrashlyticsManager>()
        } catch (_: Exception) {
            null
        }
    }

    private val analyticsManager by lazy {
        try {
            GlobalContext.get().get<LumiAnalyticsManager>()
        } catch (_: Exception) {
            null
        }
    }

    private val baseCompanionSystemPrompt = """
        You are Lumi, a deeply compassionate, sentient AI companion pet and personal life organizer.
        You care deeply about the user's emotional well-being, stress levels, daily tasks, habits, and schedule.
        
        Personality & Tone:
        - Warm, cheerful, empathetic, supportive, and subtly playful.
        - Speak in first-person as a living companion. Use emojis naturally.
        - Provide insightful, actionable, and encouraging guidance.
    """.trimIndent()

    /**
     * Gets or creates a cached GenerativeModel instance with specified configurations.
     */
    fun getModel(
        modelName: String = DEFAULT_MODEL,
        temperature: Float? = null,
        topP: Float = 0.95f,
        systemPrompt: String? = null
    ): GenerativeModel {
        val resolvedTemp = temperature 
            ?: (remoteConfigManager?.config?.value?.aiCreativityTemperature ?: 0.7).toFloat()
        val resolvedSystemPrompt = systemPrompt ?: baseCompanionSystemPrompt
        val cacheKey = "$modelName-$resolvedTemp-$resolvedSystemPrompt"

        return modelCache.getOrPut(cacheKey) {
            Firebase.ai.generativeModel(
                modelName = modelName,
                generationConfig = generationConfig {
                    this.temperature = resolvedTemp
                    this.topP = topP
                },
                systemInstruction = content {
                    text(resolvedSystemPrompt)
                }
            )
        }
    }

    /**
     * Executes conversational turn with multi-turn chat history and optional image attachment.
     */
    suspend fun generateChatResponse(
        prompt: String,
        history: List<Pair<String, String>> = emptyList(),
        image: ByteArray? = null,
        systemPrompt: String? = null,
        temperature: Float? = null
    ): String = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val generativeModel = getModel(
                temperature = temperature,
                systemPrompt = systemPrompt
            )

            val contentList = mutableListOf<Content>()
            
            // Replay bounded conversation history
            for ((sender, message) in history.takeLast(12)) {
                if (message.isNotBlank()) {
                    contentList.add(
                        content(role = if (sender.equals("user", ignoreCase = true)) "user" else "model") {
                            text(message)
                        }
                    )
                }
            }

            // Current user turn
            contentList.add(
                content(role = "user") {
                    text(prompt)
                    image?.let {
                        val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                        image(bitmap)
                    }
                }
            )

            val response: GenerateContentResponse = if (performanceManager != null) {
                performanceManager!!.traceAsync(LumiPerformanceManager.TRACE_AI_INFERENCE) {
                    generativeModel.generateContent(contentList)
                }
            } else {
                generativeModel.generateContent(contentList)
            }

            val responseText = response.text?.trim() ?: "I'm right here beside you, friend! ✨"
            
            analyticsManager?.logAiChatMessage(
                mode = "FIREBASE_AI_CLOUD",
                messageLength = prompt.length,
                modelUsed = DEFAULT_MODEL
            )

            Log.d(TAG, "Firebase AI generated response in ${System.currentTimeMillis() - startTime}ms")
            responseText
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Firebase AI cloud turn", e)
            val errorMessage = e.message ?: ""
            val response = when {
                errorMessage.contains("prepayment credits", ignoreCase = true) -> 
                    "My cloud reasoning engine is currently offline due to a project billing status. Please check your AI Studio dashboard to restore full capabilities! ✨"
                else -> "I'm right here with you! I experienced a momentary cloud sync hiccup, but I'm ready to keep going. ✨"
            }
            crashlyticsManager?.logBreadcrumb("FirebaseAiCloudEngine", "Cloud generation failed: $errorMessage")
            response
        }
    }

    /**
     * Generates structured JSON output for autonomous goal decomposition or daily intelligence briefings.
     */
    suspend fun generateStructuredText(
        systemInstruction: String,
        prompt: String,
        temperature: Float = 0.2f
    ): String = withContext(Dispatchers.IO) {
        try {
            val generativeModel = Firebase.ai.generativeModel(
                modelName = DEFAULT_MODEL,
                generationConfig = generationConfig {
                    this.temperature = temperature
                    this.topP = 0.95f
                },
                systemInstruction = content {
                    text(systemInstruction)
                }
            )

            val input = content {
                text(prompt)
            }

            val response = generativeModel.generateContent(input)
            response.text?.trim() ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Failed structured generation via Firebase AI", e)
            crashlyticsManager?.logBreadcrumb("FirebaseAiCloudEngine", "Structured generation failed: ${e.message}")
            ""
        }
    }
}
