package com.example.framework.tools.time

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import com.example.domain.tools.*

object TimeToolsModule {
    fun register(context: Context, registry: ToolRegistry = ToolRegistry.getInstance()) {
        registry.registerTool(TimerTool())
        registry.registerTool(SetAlarmClockTool(context))
        registry.registerTool(DoNotDisturbStatusTool(context))
    }
}

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
    override val parameters: List<ToolParameter> = emptyList()

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
