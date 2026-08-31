package com.example.framework.tools.audio

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import com.example.domain.tools.*

object AudioToolsModule {
    fun register(context: Context, registry: ToolRegistry = ToolRegistry.getInstance()) {
        registry.registerTool(FlashlightTool(context))
        registry.registerTool(VolumeMediaTool(context))
        registry.registerTool(VolumeRingerTool(context))
        registry.registerTool(VolumeAlarmTool(context))
        registry.registerTool(RingerModeTool(context))
    }
}

class FlashlightTool(private val context: Context) : LumiTool {
    override val id = "system_toggle_flashlight"
    override val displayName = "Toggle Flashlight"
    override val description = "Turns camera torch on or off"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = listOf(
        ToolParameter("state", "boolean", "true for ON, false for OFF", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val state = params["state"].toString().toBoolean()
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return ToolExecutionResult(false, "Camera service unavailable")
            val cameraId = cameraManager.cameraIdList.firstOrNull()
                ?: return ToolExecutionResult(false, "No camera hardware detected")
            cameraManager.setTorchMode(cameraId, state)
            ToolExecutionResult(true, "Flashlight turned ${if (state) "ON" else "OFF"}")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Flashlight error: ${e.localizedMessage}")
        }
    }
}

class VolumeMediaTool(private val context: Context) : LumiTool {
    override val id = "system_set_media_volume"
    override val displayName = "Set Media Volume"
    override val description = "Sets media volume percentage (0 to 100)"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = listOf(
        ToolParameter("level", "number", "Volume percentage (0-100)", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val level = params["level"].toString().toDoubleOrNull()?.toInt()?.coerceIn(0, 100) ?: 50
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return ToolExecutionResult(false, "Audio service unavailable")
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = (max * (level / 100.0)).toInt()
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
            ToolExecutionResult(true, "Media volume set to $level%")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Volume adjustment failed: ${e.localizedMessage}")
        }
    }
}

class VolumeRingerTool(private val context: Context) : LumiTool {
    override val id = "system_set_ringer_volume"
    override val displayName = "Set Ringer Volume"
    override val description = "Sets phone ringtone volume percentage (0 to 100)"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = listOf(
        ToolParameter("level", "number", "Ringer percentage (0-100)", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val level = params["level"].toString().toDoubleOrNull()?.toInt()?.coerceIn(0, 100) ?: 50
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return ToolExecutionResult(false, "Audio service unavailable")
            val max = am.getStreamMaxVolume(AudioManager.STREAM_RING)
            val target = (max * (level / 100.0)).toInt()
            am.setStreamVolume(AudioManager.STREAM_RING, target, 0)
            ToolExecutionResult(true, "Ringer volume set to $level%")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Ringer adjustment failed: ${e.localizedMessage}")
        }
    }
}

class VolumeAlarmTool(private val context: Context) : LumiTool {
    override val id = "system_set_alarm_volume"
    override val displayName = "Set Alarm Volume"
    override val description = "Sets alarm volume percentage (0 to 100)"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = listOf(
        ToolParameter("level", "number", "Alarm volume percentage (0-100)", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val level = params["level"].toString().toDoubleOrNull()?.toInt()?.coerceIn(0, 100) ?: 50
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return ToolExecutionResult(false, "Audio service unavailable")
            val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val target = (max * (level / 100.0)).toInt()
            am.setStreamVolume(AudioManager.STREAM_ALARM, target, 0)
            ToolExecutionResult(true, "Alarm volume set to $level%")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Alarm volume failed: ${e.localizedMessage}")
        }
    }
}

class RingerModeTool(private val context: Context) : LumiTool {
    override val id = "system_set_ringer_mode"
    override val displayName = "Set Ringer Mode"
    override val description = "Sets sound profile mode: NORMAL, VIBRATE, or SILENT"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = listOf(
        ToolParameter("mode", "string", "NORMAL, VIBRATE, or SILENT", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val modeStr = params["mode"].toString().uppercase()
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return ToolExecutionResult(false, "Audio service unavailable")
            when (modeStr) {
                "SILENT" -> am.ringerMode = AudioManager.RINGER_MODE_SILENT
                "VIBRATE" -> am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                else -> am.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
            ToolExecutionResult(true, "Ringer mode set to $modeStr")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Failed to set ringer mode: ${e.localizedMessage}")
        }
    }
}
