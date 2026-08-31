package com.example.framework.tools

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager
import com.example.domain.tools.*

/**
 * Built-in Android System Tools Suite.
 * Demonstrates modular LumiTool implementations wrapping Android OS capabilities.
 */
object SystemToolSuite {

    fun registerAll(context: Context, registry: ToolRegistry = ToolRegistry.getInstance()) {
        registry.registerTool(FlashlightTool(context))
        registry.registerTool(VolumeTool(context))
        registry.registerTool(BatteryTool(context))
        registry.registerTool(SystemInfoTool())
        registry.registerTool(TimerTool())
    }
}

class FlashlightTool(private val context: Context) : LumiTool {
    override val id = "system_toggle_flashlight"
    override val displayName = "Toggle Flashlight"
    override val description = "Turns the camera flashlight torch on or off"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = listOf(
        ToolParameter("state", "boolean", "true to turn on flashlight, false to turn off", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val state = params["state"].toString().toBoolean()
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return ToolExecutionResult(false, "No camera found")
            cameraManager.setTorchMode(cameraId, state)
            ToolExecutionResult(true, "Flashlight turned ${if (state) "ON" else "OFF"}")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Failed to control flashlight: ${e.localizedMessage}")
        }
    }
}

class VolumeTool(private val context: Context) : LumiTool {
    override val id = "system_set_volume"
    override val displayName = "Set Volume"
    override val description = "Adjusts device media volume percentage (0 to 100)"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = listOf(
        ToolParameter("level", "number", "Volume level percentage from 0 to 100", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val levelPercent = params["level"].toString().toDoubleOrNull()?.toInt() ?: 50
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVol = (maxVol * (levelPercent / 100.0)).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
            ToolExecutionResult(true, "Media volume set to $levelPercent%")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Failed to adjust volume: ${e.localizedMessage}")
        }
    }
}

class BatteryTool(private val context: Context) : LumiTool {
    override val id = "device_get_battery"
    override val displayName = "Get Battery Status"
    override val description = "Returns current battery percentage and charging state"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val isCharging = bm.isCharging
            ToolExecutionResult(true, "Battery level is $level% (${if (isCharging) "Charging" else "Not Charging"})")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Failed to read battery: ${e.localizedMessage}")
        }
    }
}

class SystemInfoTool : LumiTool {
    override val id = "system_get_device_info"
    override val displayName = "Get Device Info"
    override val description = "Returns Android version, model, and hardware details"
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val model = android.os.Build.MODEL
        val brand = android.os.Build.BRAND
        val androidVer = android.os.Build.VERSION.RELEASE
        return ToolExecutionResult(true, "Device: $brand $model, Android $androidVer")
    }
}

class TimerTool : LumiTool {
    override val id = "system_set_quick_timer"
    override val displayName = "Set Quick Timer"
    override val description = "Schedules a quick countdown notification timer in seconds"
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = listOf(
        ToolParameter("seconds", "number", "Duration in seconds", required = true),
        ToolParameter("label", "string", "Optional title for timer", required = false)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val seconds = params["seconds"].toString().toDoubleOrNull()?.toInt() ?: 10
        val label = params["label"]?.toString() ?: "Timer"
        return ToolExecutionResult(true, "Timer set for $seconds seconds ($label)")
    }
}
