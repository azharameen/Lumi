package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class LumiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "lumi_companion_service",
                "Lumi Companion Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows floating Lumi pet companion overlay status"
            }

            val reminderChannel = NotificationChannel(
                "lumi_reminders",
                "Lumi Daily Check-Ins",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily wellness, mindfulness and schedule reminders from Lumi"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            manager.createNotificationChannel(reminderChannel)
        }
    }
}
