package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

import com.example.core.di.appModule
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import android.util.Log
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LumiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        initializeFirebase()

        startKoin {
            androidLogger()
            androidContext(this@LumiApplication)
            modules(appModule)
        }
        
        createNotificationChannels()
    }

    private fun initializeFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                if (app == null) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:663377968514:android:bac0ab54e860f4ed40639b")
                        .setApiKey("AIzaSyBwusrDv4IY6WvoP7Nqb5FKn3DcdmxYiXk")
                        .setProjectId("studio-8325749739-eefac")
                        .setDatabaseUrl("https://studio-8325749739-eefac-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .setStorageBucket("studio-8325749739-eefac.firebasestorage.app")
                        .setGcmSenderId("663377968514")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                    Log.i("LumiApp", "Firebase initialized with explicit fallback options")
                } else {
                    Log.i("LumiApp", "Firebase initialized automatically from google-services")
                }
            }
        } catch (e: Exception) {
            Log.e("LumiApp", "Error during Firebase initialization", e)
        }
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
