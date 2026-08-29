package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.local.LumiDatabase
import com.example.data.repository.LumiRepositoryImpl
import com.example.domain.repository.LumiRepository
import com.example.ui.overlay.PetOverlayRoot
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Foreground Android Service that displays the floating Lumi Companion Overlay over other apps.
 * Manages WindowManager layout params, roaming animation ticks, and service lifecycle.
 */
class PetOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayComposeView: ComposeView? = null
    private lateinit var windowLayoutParams: WindowManager.LayoutParams
    private val lifecycleOwner = OverlayLifecycleOwner()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var roamJob: Job? = null

    private lateinit var repository: LumiRepository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner.onCreate()
        lifecycleOwner.onResume()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Initialize Shared Singleton Repository
        repository = LumiRepositoryImpl.getInstance(applicationContext)
        repository.setOverlayActive(true)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        setupOverlayWindow()
    }

    private fun setupOverlayWindow() {
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        windowLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 300
        }

        overlayComposeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)

            setContent {
                MyApplicationTheme {
                    PetOverlayRoot(
                        context = this@PetOverlayService,
                        repository = repository,
                        onMoveOverlay = { deltaX, deltaY ->
                            windowLayoutParams.x = (windowLayoutParams.x + deltaX.roundToInt()).coerceAtLeast(0)
                            windowLayoutParams.y = (windowLayoutParams.y + deltaY.roundToInt()).coerceAtLeast(0)
                            windowManager.updateViewLayout(overlayComposeView, windowLayoutParams)
                        },
                        onCloseService = {
                            stopSelf()
                        },
                        onToggleRoamMode = { isRoamEnabled ->
                            setRoamMode(isRoamEnabled)
                        }
                    )
                }
            }
        }

        windowManager.addView(overlayComposeView, windowLayoutParams)
    }

    private fun setRoamMode(enable: Boolean) {
        roamJob?.cancel()
        if (enable) {
            roamJob = serviceScope.launch {
                while (isActive) {
                    delay(Random.nextLong(4000, 9000))
                    val moveX = Random.nextInt(-45, 45)
                    val moveY = Random.nextInt(-45, 45)
                    windowLayoutParams.x = (windowLayoutParams.x + moveX).coerceIn(40, 750)
                    windowLayoutParams.y = (windowLayoutParams.y + moveY).coerceIn(120, 1600)
                    overlayComposeView?.let { windowManager.updateViewLayout(it, windowLayoutParams) }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lumi Floating Companion",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Lumi is keeping you company and organizing your day"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lumi Companion Active")
            .setContentText("Your AI pet is floating by your side 🌸")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::repository.isInitialized) {
            repository.setOverlayActive(false)
        }
        serviceScope.cancel()
        lifecycleOwner.onPause()
        lifecycleOwner.onDestroy()
        overlayComposeView?.let {
            windowManager.removeView(it)
            overlayComposeView = null
        }
    }

    companion object {
        private const val CHANNEL_ID = "lumi_overlay_channel"
        private const val NOTIFICATION_ID = 2001
    }
}
