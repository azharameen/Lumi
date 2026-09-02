package com.example.framework.tools.system

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.domain.tools.*

object SystemToolsModule {
    fun register(context: Context, registry: ToolRegistry = ToolRegistry.getInstance()) {
        registry.registerTool(BatteryTool(context))
        registry.registerTool(StorageInfoTool())
        registry.registerTool(RamUsageTool(context))
        registry.registerTool(SystemInfoTool())
        registry.registerTool(DeviceUptimeTool())
        registry.registerTool(HapticFeedbackTool(context))
        registry.registerTool(PostPetNotificationTool(context))
        registry.registerTool(NetworkStatusTool(context))
        registry.registerTool(com.example.framework.tools.security.BiometricCheckTool(context))
        registry.registerTool(com.example.framework.tools.location.RealLocationTool(context))
    }
}

class BatteryTool(private val context: Context) : LumiTool {
    override val id = "device_get_battery"
    override val displayName = "Get Battery Info"
    override val description = "Reads battery level, charging status, and power saver state"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            val isCharging = bm?.isCharging == true
            val isPowerSave = pm?.isPowerSaveMode == true
            
            val statusStr = "Battery: $level% (${if (isCharging) "Charging" else "Discharging"}), Battery Saver: ${if (isPowerSave) "ON" else "OFF"}"
            ToolExecutionResult(true, statusStr)
        } catch (e: Exception) {
            ToolExecutionResult(false, "Battery query error: ${e.localizedMessage}")
        }
    }
}

class StorageInfoTool : LumiTool {
    override val id = "device_get_storage_info"
    override val displayName = "Get Internal Storage Info"
    override val description = "Checks total and available internal storage space"
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            val totalBytes = stat.blockCountLong * stat.blockSizeLong
            val freeGb = String.format("%.1f", availableBytes / (1024.0 * 1024.0 * 1024.0))
            val totalGb = String.format("%.1f", totalBytes / (1024.0 * 1024.0 * 1024.0))
            ToolExecutionResult(true, "Internal Storage: $freeGb GB free out of $totalGb GB")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Storage info failed: ${e.localizedMessage}")
        }
    }
}

class RamUsageTool(private val context: Context) : LumiTool {
    override val id = "device_get_ram_usage"
    override val displayName = "Get Memory (RAM) Usage"
    override val description = "Checks total and available physical RAM"
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
                ?: return ToolExecutionResult(false, "ActivityManager unavailable")
            val memInfo = android.app.ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)
            val availGb = String.format("%.1f", memInfo.availMem / (1024.0 * 1024.0 * 1024.0))
            val totalGb = String.format("%.1f", memInfo.totalMem / (1024.0 * 1024.0 * 1024.0))
            ToolExecutionResult(true, "Device RAM: $availGb GB free out of $totalGb GB (Low RAM state: ${memInfo.lowMemory})")
        } catch (e: Exception) {
            ToolExecutionResult(false, "RAM check failed: ${e.localizedMessage}")
        }
    }
}

class SystemInfoTool : LumiTool {
    override val id = "system_get_device_info"
    override val displayName = "Get Device Info"
    override val description = "Returns Android model, brand, SDK version, and device name"
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val model = Build.MODEL
        val brand = Build.BRAND
        val release = Build.VERSION.RELEASE
        val sdk = Build.VERSION.SDK_INT
        return ToolExecutionResult(true, "Device: $brand $model (Android $release, API $sdk)")
    }
}

class DeviceUptimeTool : LumiTool {
    override val id = "device_get_uptime"
    override val displayName = "Get Device Uptime"
    override val description = "Checks how long the device has been running without a reboot"
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val uptimeMs = SystemClock.elapsedRealtime()
        val hours = uptimeMs / (1000 * 60 * 60)
        val minutes = (uptimeMs / (1000 * 60)) % 60
        return ToolExecutionResult(true, "Device Uptime: $hours hours, $minutes minutes")
    }
}

class HapticFeedbackTool(private val context: Context) : LumiTool {
    override val id = "device_trigger_haptic_feedback"
    override val displayName = "Trigger Haptic Vibrate"
    override val description = "Vibrates phone using pet pattern: LIGHT, MEDIUM, or HEAVY"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = listOf(
        ToolParameter("pattern", "string", "LIGHT, MEDIUM, or HEAVY", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val patternStr = params["pattern"].toString().uppercase()
            val durationMs = when (patternStr) {
                "LIGHT" -> 50L
                "HEAVY" -> 300L
                else -> 150L
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vm?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
            ToolExecutionResult(true, "Triggered $patternStr haptic feedback ($durationMs ms)")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Vibration failed: ${e.localizedMessage}")
        }
    }
}

class PostPetNotificationTool(private val context: Context) : LumiTool {
    override val id = "notification_post_pet_alert"
    override val displayName = "Post Pet Alert Notification"
    override val description = "Displays a push notification from Lumi in the Android status bar"
    override val category = ToolCategory.COMMUNICATION
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = listOf(
        ToolParameter("title", "string", "Title of notification", required = true),
        ToolParameter("message", "string", "Body text of notification", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val title = params["title"]?.toString() ?: "Lumi Alert"
            val message = params["message"]?.toString() ?: "Woof! You have a new notification."
            val channelId = "lumi_pet_alerts_channel"

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return ToolExecutionResult(false, "NotificationManager unavailable")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "Lumi Pet Alerts", NotificationManager.IMPORTANCE_DEFAULT)
                nm.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            nm.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
            ToolExecutionResult(true, "Posted notification: '$title'")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Notification post failed: ${e.localizedMessage}")
        }
    }
}

class NetworkStatusTool(private val context: Context) : LumiTool {
    override val id = "network_get_status"
    override val displayName = "Get Network Connection Status"
    override val description = "Checks active internet connection type (Wi-Fi, Cellular, or Offline)"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return ToolExecutionResult(false, "ConnectivityManager unavailable")
            val activeNetwork = cm.activeNetwork
            val caps = cm.getNetworkCapabilities(activeNetwork)
            
            val connectionType = when {
                caps == null -> "Offline"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi Connected"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular Data Connected"
                else -> "Connected"
            }
            ToolExecutionResult(true, "Network Status: $connectionType")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Network status failed: ${e.localizedMessage}")
        }
    }
}
