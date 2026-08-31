package com.example.framework.tools.media

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import com.example.domain.tools.*

object MediaToolsModule {
    fun register(context: Context, registry: ToolRegistry = ToolRegistry.getInstance()) {
        registry.registerTool(MediaPlayPauseTool(context))
        registry.registerTool(MediaNextTrackTool(context))
        registry.registerTool(MediaPreviousTrackTool(context))
    }
}

class MediaPlayPauseTool(private val context: Context) : LumiTool {
    override val id = "media_play_pause"
    override val displayName = "Play / Pause Media"
    override val description = "Toggles music or video playback in Spotify, YouTube, or background media players"
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return ToolExecutionResult(false, "Audio service unavailable")
            val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            val eventUp = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            am.dispatchMediaKeyEvent(eventDown)
            am.dispatchMediaKeyEvent(eventUp)
            ToolExecutionResult(true, "Triggered Media Play/Pause")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Media control failed: ${e.localizedMessage}")
        }
    }
}

class MediaNextTrackTool(private val context: Context) : LumiTool {
    override val id = "media_next_track"
    override val displayName = "Next Track"
    override val description = "Skips to next track in active media player"
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return ToolExecutionResult(false, "Audio service unavailable")
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT))
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT))
            ToolExecutionResult(true, "Skipped to next track")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Next track failed: ${e.localizedMessage}")
        }
    }
}

class MediaPreviousTrackTool(private val context: Context) : LumiTool {
    override val id = "media_previous_track"
    override val displayName = "Previous Track"
    override val description = "Goes to previous track in active media player"
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return ToolExecutionResult(false, "Audio service unavailable")
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
            ToolExecutionResult(true, "Skipped to previous track")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Previous track failed: ${e.localizedMessage}")
        }
    }
}
