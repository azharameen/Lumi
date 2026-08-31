package com.example.framework

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.LumiDatabase
import com.example.data.repository.LumiRepositoryImpl
import com.example.domain.model.PetEmotion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for handling precise scheduling reminders, wellness hydration crons,
 * and system boot alarms.
 */
class LumiAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val notificationManager = NotificationManagerCompat.from(context)

        when (action) {
            ACTION_SCHEDULE_REMINDER -> {
                val title = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: "Upcoming Schedule Event"
                val eventId = intent.getLongExtra(EXTRA_EVENT_ID, 0L)
                val description = intent.getStringExtra(EXTRA_EVENT_DESC) ?: "Lumi is keeping you on track!"

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("NAVIGATE_TAB", 2) // Schedule tab
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    eventId.toInt(),
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("⏰ Lumi Alert: $title")
                    .setContentText(description)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                try {
                    notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
                } catch (e: SecurityException) {
                    // Ignored if notification permission is not granted
                }
            }

            ACTION_WELLNESS_NUDGE -> {
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("NAVIGATE_TAB", 4) // Wellness tab
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    2002,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("💧 Lumi Wellness Check")
                    .setContentText("Time for a sip of water and a deep breath with Lumi!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                try {
                    notificationManager.notify(2002, notification)
                } catch (e: SecurityException) {
                    // Ignored
                }
                LumiAlarmScheduler.scheduleNextWellnessNudge(context)
            }

            ACTION_MORNING_BRIEFING -> {
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("OPEN_BRIEFING", "MORNING")
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    4001,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("🌅 Lumi Morning Intelligence Briefing")
                    .setContentText("Good morning! Tap to view your daily schedule, top goals & audio briefing.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                try {
                    notificationManager.notify(4001, notification)
                } catch (e: SecurityException) {
                    // Ignored
                }
                LumiAlarmScheduler.scheduleDailyBriefings(context)
            }

            ACTION_EVENING_BRIEFING -> {
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("OPEN_BRIEFING", "EVENING")
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    4002,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("🌙 Lumi Evening Reflection")
                    .setContentText("Great job today! Review your achievements and wind down with Lumi.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                try {
                    notificationManager.notify(4002, notification)
                } catch (e: SecurityException) {
                    // Ignored
                }
                LumiAlarmScheduler.scheduleDailyBriefings(context)
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                // Reschedule upcoming alarms on boot
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                scope.launch {
                    val repository = org.koin.core.context.GlobalContext.get().get<com.example.domain.repository.LumiRepository>()
                    // Re-schedule alarms
                    LumiAlarmScheduler.scheduleNextWellnessNudge(context)
                    LumiAlarmScheduler.scheduleDailyBriefings(context)
                }
            }
        }
    }

    companion object {
        const val ACTION_SCHEDULE_REMINDER = "com.example.lumi.ACTION_SCHEDULE_REMINDER"
        const val ACTION_WELLNESS_NUDGE = "com.example.lumi.ACTION_WELLNESS_NUDGE"
        const val ACTION_MORNING_BRIEFING = "com.example.lumi.ACTION_MORNING_BRIEFING"
        const val ACTION_EVENING_BRIEFING = "com.example.lumi.ACTION_EVENING_BRIEFING"
        const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        const val EXTRA_EVENT_TITLE = "EXTRA_EVENT_TITLE"
        const val EXTRA_EVENT_DESC = "EXTRA_EVENT_DESC"
        const val CHANNEL_REMINDERS = "lumi_reminders_channel"
    }
}

/**
 * Helper to register and cancel exact AlarmManager alarms.
 */
object LumiAlarmScheduler {

    fun scheduleEventAlarm(context: Context, eventId: Long, title: String, description: String, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, LumiAlarmReceiver::class.java).apply {
            action = LumiAlarmReceiver.ACTION_SCHEDULE_REMINDER
            putExtra(LumiAlarmReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(LumiAlarmReceiver.EXTRA_EVENT_TITLE, title)
            putExtra(LumiAlarmReceiver.EXTRA_EVENT_DESC, description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: Exception) {
            // Fallback for security restrictions
        }
    }

    fun cancelEventAlarm(context: Context, eventId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, LumiAlarmReceiver::class.java).apply {
            action = LumiAlarmReceiver.ACTION_SCHEDULE_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun scheduleNextWellnessNudge(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, LumiAlarmReceiver::class.java).apply {
            action = LumiAlarmReceiver.ACTION_WELLNESS_NUDGE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            3003,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000L) // in 2 hours
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun scheduleDailyBriefings(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Morning briefing at 8:00 AM
        val morningCal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 8)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }

        val morningIntent = Intent(context, LumiAlarmReceiver::class.java).apply {
            action = LumiAlarmReceiver.ACTION_MORNING_BRIEFING
        }
        val morningPendingIntent = PendingIntent.getBroadcast(
            context,
            4001,
            morningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Evening reflection at 9:00 PM (21:00)
        val eveningCal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 21)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }

        val eveningIntent = Intent(context, LumiAlarmReceiver::class.java).apply {
            action = LumiAlarmReceiver.ACTION_EVENING_BRIEFING
        }
        val eveningPendingIntent = PendingIntent.getBroadcast(
            context,
            4002,
            eveningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, morningCal.timeInMillis, morningPendingIntent)
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, eveningCal.timeInMillis, eveningPendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, morningCal.timeInMillis, morningPendingIntent)
                alarmManager.set(AlarmManager.RTC_WAKEUP, eveningCal.timeInMillis, eveningPendingIntent)
            }
        } catch (e: Exception) {
            // Ignored
        }
    }
}




