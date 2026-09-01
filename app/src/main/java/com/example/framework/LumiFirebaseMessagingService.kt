package com.example.framework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.firebase.LumiAnalyticsManager
import com.example.data.firebase.LumiCrashlyticsManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

/**
 * Firebase Cloud Messaging Service handling real-time push alerts,
 * proactive companion nudges, and remote mindful reminders.
 */
class LumiFirebaseMessagingService : FirebaseMessagingService() {

    private val crashlytics: LumiCrashlyticsManager by inject()
    private val analytics: LumiAnalyticsManager by inject()

    companion object {
        private const val TAG = "LumiFCMService"
        const val CHANNEL_PROACTIVE_ALERTS = "lumi_proactive_alerts_channel"

        /**
         * Subscribes the device to common companion notification topics.
         */
        fun subscribeToCompanionTopics() {
            try {
                // Check if Google Play Services is likely available before subscribing 
                // to prevent FCM Registration hard failure exceptions on emulators.
                val isEmulator = (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                        || android.os.Build.FINGERPRINT.startsWith("generic")
                        || android.os.Build.FINGERPRINT.startsWith("unknown")
                        || android.os.Build.HARDWARE.contains("goldfish")
                        || android.os.Build.HARDWARE.contains("ranchu")
                        || android.os.Build.HARDWARE.contains("cutf_cvm")
                        || android.os.Build.MODEL.contains("google_sdk")
                        || android.os.Build.MODEL.contains("Emulator")
                        || android.os.Build.MODEL.contains("Android SDK built for x86")
                        || android.os.Build.MANUFACTURER.contains("Genymotion")
                        || android.os.Build.PRODUCT.contains("sdk_google")
                        || android.os.Build.PRODUCT.contains("google_sdk")
                        || android.os.Build.PRODUCT.contains("sdk")
                        || android.os.Build.PRODUCT.contains("sdk_x86")
                        || android.os.Build.PRODUCT.contains("vbox86p")
                        || android.os.Build.PRODUCT.contains("emulator")
                        || android.os.Build.PRODUCT.contains("simulator")

                if (isEmulator) {
                    Log.d(TAG, "Running on emulator, skipping FCM topic subscription to prevent hard failures.")
                    return
                }

                FirebaseMessaging.getInstance().subscribeToTopic("companion_proactive_alerts")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Subscribed to topic: companion_proactive_alerts")
                        } else {
                            Log.w(TAG, "Failed to subscribe to topic", task.exception)
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error subscribing to FCM topics", e)
            }
        }

        /**
         * Sends a test companion notification locally so the user can verify formatting and tab routing.
         */
        fun sendLocalTestNotification(
            context: Context,
            title: String = "✨ Lumi: Mindful Moment",
            body: String = "Your companion is ready! Tap to check in on today's goals and mindful reset.",
            targetTab: Int = 1,
            alertType: String = "test_companion_ping"
        ) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_PROACTIVE_ALERTS,
                    "Lumi Proactive Companion Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Proactive check-ins, mindful nudges, and real-time alerts from your companion Lumi"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TAB", targetTab)
                putExtra("FCM_ALERT_TYPE", alertType)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                (System.currentTimeMillis() % 10000).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_PROACTIVE_ALERTS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(
                    android.R.drawable.ic_menu_send,
                    "Check In",
                    pendingIntent
                )
                .build()

            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            try {
                notificationManager.notify(notificationId, notification)
            } catch (e: SecurityException) {
                Log.w(TAG, "Notification permission not granted for test alert", e)
            }
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM Registration Token: $token")
        crashlytics.log("FCM token refreshed: ${token.take(10)}...")
        
        // Subscribe to global companion alerts topic
        subscribeToCompanionTopics()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Received FCM message from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notificationPayload = remoteMessage.notification

        val title = notificationPayload?.title
            ?: data["title"]
            ?: "✨ Lumi Companion"

        val body = notificationPayload?.body
            ?: data["body"]
            ?: data["message"]
            ?: "Your companion has a gentle mindful update for you!"

        val alertType = data["alert_type"] ?: "proactive_checkin"
        val targetTab = data["target_tab"]?.toIntOrNull() ?: 0 // 0=Chat, 1=Pet, 2=Goals, 3=LifeHub, 4=Wellness

        crashlytics.log("FCM message received: alert_type=$alertType, tab=$targetTab")
        analytics.logScreenView("FCM_Notification_$alertType")

        showCompanionNotification(
            title = title,
            body = body,
            targetTab = targetTab,
            alertType = alertType
        )
    }

    private fun showCompanionNotification(
        title: String,
        body: String,
        targetTab: Int,
        alertType: String
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TAB", targetTab)
            putExtra("FCM_ALERT_TYPE", alertType)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_PROACTIVE_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission not granted for FCM alert", e)
        }
    }

    private fun createNotificationChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_PROACTIVE_ALERTS,
                "Lumi Proactive Companion Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Proactive check-ins, mindful nudges, and real-time alerts from your companion Lumi"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
