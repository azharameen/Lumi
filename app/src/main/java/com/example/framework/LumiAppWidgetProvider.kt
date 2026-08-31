package com.example.framework

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.LumiDatabase
import com.example.data.repository.LumiRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Native Android Home Screen App Widget for Lumi.
 * Provides live glanceable mood, top task, and interactive quick action intents.
 */
class LumiAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidgetView(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_QUICK_HYDRATE) {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            scope.launch {
                val repo = org.koin.java.KoinJavaComponent.getKoin().get(com.example.domain.repository.LumiRepository::class.java)
                repo.logWellness(8, "Hydrated via Widget", 8, 1, "Quick widget tap")
                triggerWidgetUpdate(context)
            }
        } else if (intent.action == ACTION_WIDGET_PET) {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            scope.launch {
                val repo = org.koin.java.KoinJavaComponent.getKoin().get(com.example.domain.repository.LumiRepository::class.java)
                repo.petTheCharacter()
                triggerWidgetUpdate(context)
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_QUICK_HYDRATE = "com.example.lumi.ACTION_WIDGET_QUICK_HYDRATE"
        const val ACTION_WIDGET_PET = "com.example.lumi.ACTION_WIDGET_PET"

        fun triggerWidgetUpdate(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context.packageName, LumiAppWidgetProvider::class.java.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            for (widgetId in appWidgetIds) {
                updateWidgetView(context, appWidgetManager, widgetId)
            }
        }

        private fun updateWidgetView(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.lumi_app_widget)

            // Open app on main click
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, openPendingIntent)

            // Quick hydrate intent
            val hydrateIntent = Intent(context, LumiAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_QUICK_HYDRATE
            }
            val hydratePending = PendingIntent.getBroadcast(
                context,
                101,
                hydrateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_water, hydratePending)

            // Quick pet intent
            val petIntent = Intent(context, LumiAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_PET
            }
            val petPending = PendingIntent.getBroadcast(
                context,
                102,
                petIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_pet, petPending)

            // Asynchronously load real-time status and update
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            scope.launch {
                try {
                    val repo = org.koin.java.KoinJavaComponent.getKoin().get(com.example.domain.repository.LumiRepository::class.java)
                    val pet = repo.petStatus.firstOrNull()
                    val tasks = repo.allTasks.firstOrNull()
                    val topTask = tasks?.firstOrNull { !it.isCompleted }?.title ?: "All tasks completed! ✨"

                    val levelText = "Lumi Lv.${pet?.level ?: 1} • ${pet?.currentEmotion?.displayName ?: "Happy"}"
                    views.setTextViewText(R.id.widget_title, levelText)
                    views.setTextViewText(R.id.widget_task_text, "🎯 $topTask")
                    views.setTextViewText(R.id.widget_xp_text, "XP: ${pet?.exp ?: 0}/${pet?.expToNextLevel ?: 100} • Bond: ${pet?.bondScore ?: 50}%")

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}

