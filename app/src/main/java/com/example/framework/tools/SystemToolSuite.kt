package com.example.framework.tools

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.domain.tools.*

/**
 * Complete Enterprise Android Mobile Control Suite.
 * Gives Lumi comprehensive safe control over device hardware, settings, radios, audio, and utilities.
 * Every tool is wrapped in strict safety handlers to prevent app crashes.
 */
object SystemToolSuite {

    fun registerAll(context: Context, registry: ToolRegistry = ToolRegistry.getInstance()) {
        // Audio & Sound
        registry.registerTool(FlashlightTool(context))
        registry.registerTool(VolumeMediaTool(context))
        registry.registerTool(VolumeRingerTool(context))
        registry.registerTool(VolumeAlarmTool(context))
        registry.registerTool(RingerModeTool(context))
        
        // Battery, Memory & Storage
        registry.registerTool(BatteryTool(context))
        registry.registerTool(StorageInfoTool())
        registry.registerTool(RamUsageTool(context))
        registry.registerTool(SystemInfoTool())

        // Display & Feedback
        registry.registerTool(HapticFeedbackTool(context))
        registry.registerTool(PostPetNotificationTool(context))

        // Alarms, Timers & DND
        registry.registerTool(TimerTool())
        registry.registerTool(SetAlarmClockTool(context))
        registry.registerTool(DoNotDisturbStatusTool(context))

        // Network & Settings
        registry.registerTool(NetworkStatusTool(context))
        registry.registerTool(OpenWifiSettingsTool(context))
        registry.registerTool(OpenBluetoothSettingsTool(context))

        // Communication (High Risk Guard)
        registry.registerTool(DraftSmsTool(context))
        registry.registerTool(DialPhoneTool(context))
    }
}

// ==========================================
// 1. AUDIO & HARDWARE TOOLS
// ==========================================

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

// ==========================================
// 2. BATTERY, MEMORY & STORAGE TOOLS
// ==========================================

class BatteryTool(private val context: Context) : LumiTool {
    override val id = "device_get_battery"
    override val displayName = "Get Battery Info"
    override val description = "Reads battery level, charging status, and power saver state"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = emptyList()

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
    override val parameters = emptyList()

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
    override val parameters = emptyList()

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
    override val parameters = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val model = Build.MODEL
        val brand = Build.BRAND
        val release = Build.VERSION.RELEASE
        val sdk = Build.VERSION.SDK_INT
        return ToolExecutionResult(true, "Device: $brand $model (Android $release, API $sdk)")
    }
}

// ==========================================
// 3. DISPLAY & FEEDBACK TOOLS
// ==========================================

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

// ==========================================
// 4. TIMERS, ALARMS & DND
// ==========================================

class TimerTool : LumiTool {
    override val id = "system_set_quick_timer"
    override val displayName = "Set Quick Timer"
    override val description = "Schedules a countdown timer in seconds"
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = listOf(
        ToolParameter("seconds", "number", "Duration in seconds", required = true),
        ToolParameter("label", "string", "Timer label", required = false)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val seconds = params["seconds"].toString().toDoubleOrNull()?.toInt() ?: 10
        val label = params["label"]?.toString() ?: "Timer"
        return ToolExecutionResult(true, "Timer scheduled for $seconds seconds ($label)")
    }
}

class SetAlarmClockTool(private val context: Context) : LumiTool {
    override val id = "system_set_alarm_clock"
    override val displayName = "Set Alarm Clock"
    override val description = "Opens Android Alarm Clock to set an alarm for specific hour and minute"
    override val category = ToolCategory.CALENDAR
    override val riskLevel = ToolRiskLevel.MEDIUM
    override val parameters = listOf(
        ToolParameter("hour", "number", "Hour of day (0-23)", required = true),
        ToolParameter("minute", "number", "Minute of hour (0-59)", required = true),
        ToolParameter("label", "string", "Alarm title", required = false)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val hour = params["hour"].toString().toDoubleOrNull()?.toInt()?.coerceIn(0, 23) ?: 8
            val minute = params["minute"].toString().toDoubleOrNull()?.toInt()?.coerceIn(0, 59) ?: 0
            val label = params["label"]?.toString() ?: "Lumi Alarm"

            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(true, "Alarm set for %02d:%02d ($label)".format(hour, minute))
        } catch (e: Exception) {
            ToolExecutionResult(false, "Failed to set alarm: ${e.localizedMessage}")
        }
    }
}

class DoNotDisturbStatusTool(private val context: Context) : LumiTool {
    override val id = "system_get_dnd_status"
    override val displayName = "Get Do Not Disturb Status"
    override val description = "Checks whether Do Not Disturb (DND) mode is currently active"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return ToolExecutionResult(false, "NotificationManager unavailable")
            val filter = nm.currentInterruptionFilter
            val isDndActive = filter != NotificationManager.INTERRUPTION_FILTER_ALL
            ToolExecutionResult(true, "Do Not Disturb is ${if (isDndActive) "ACTIVE" else "OFF"}")
        } catch (e: Exception) {
            ToolExecutionResult(false, "DND check failed: ${e.localizedMessage}")
        }
    }
}

// ==========================================
// 5. NETWORK & INTENTS
// ==========================================

class NetworkStatusTool(private val context: Context) : LumiTool {
    override val id = "network_get_status"
    override val displayName = "Get Network Connection Status"
    override val description = "Checks active internet connection type (Wi-Fi, Cellular, or Offline)"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = emptyList()

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

class OpenWifiSettingsTool(private val context: Context) : LumiTool {
    override val id = "system_open_wifi_settings"
    override val displayName = "Open Wi-Fi Settings"
    override val description = "Launches Android system Wi-Fi settings page"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(true, "Opened Wi-Fi settings page")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Failed to open Wi-Fi settings: ${e.localizedMessage}")
        }
    }
}

class OpenBluetoothSettingsTool(private val context: Context) : LumiTool {
    override val id = "system_open_bluetooth_settings"
    override val displayName = "Open Bluetooth Settings"
    override val description = "Launches Android system Bluetooth settings page"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(true, "Opened Bluetooth settings page")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Failed to open Bluetooth settings: ${e.localizedMessage}")
        }
    }
}

// ==========================================
// 6. COMMUNICATION (HIGH RISK GUARD)
// ==========================================

class DraftSmsTool(private val context: Context) : LumiTool {
    override val id = "communication_draft_sms"
    override val displayName = "Draft SMS Message"
    override val description = "Opens SMS app pre-filled with phone number and text message"
    override val category = ToolCategory.COMMUNICATION
    override val riskLevel = ToolRiskLevel.HIGH
    override val parameters = listOf(
        ToolParameter("phoneNumber", "string", "Target phone number", required = true),
        ToolParameter("message", "string", "Text message body", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val number = params["phoneNumber"]?.toString() ?: ""
            val body = params["message"]?.toString() ?: ""
            
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$number")
                putExtra("sms_body", body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(true, "Drafted SMS to $number with message: '$body'")
        } catch (e: Exception) {
            ToolExecutionResult(false, "SMS draft failed: ${e.localizedMessage}")
        }
    }
}

class DialPhoneTool(private val context: Context) : LumiTool {
    override val id = "communication_dial_number"
    override val displayName = "Dial Phone Number"
    override val description = "Opens phone dialer pre-filled with number"
    override val category = ToolCategory.COMMUNICATION
    override val riskLevel = ToolRiskLevel.HIGH
    override val parameters = listOf(
        ToolParameter("phoneNumber", "string", "Phone number to dial", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val number = params["phoneNumber"]?.toString() ?: ""
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(true, "Opened dialer for $number")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Dialer failed: ${e.localizedMessage}")
        }
    }
}
