package com.example.service

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
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
 * Features:
 * - Free floating anywhere on screen without sticky snapping on release.
 * - Always 100% visible on screen without clipping off-screen borders.
 * - Automatic smooth glide to edge margin during idle periods so content isn't obstructed.
 * - Remains fully clickable and interactive on screen at all times.
 */
class PetOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayComposeView: ComposeView? = null
    private lateinit var windowLayoutParams: WindowManager.LayoutParams
    private val lifecycleOwner = OverlayLifecycleOwner()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var roamJob: Job? = null
    private var autoHideJob: Job? = null
    private var glideAnimator: ValueAnimator? = null

    private lateinit var repository: LumiRepository

    private var isDockedPeeking = false
    private var isHubOpen = false

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

    private var initialWindowX = 0
    private var initialWindowY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDraggingOverlay = false

    private fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    private fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
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
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 16
            y = 360
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
                        isDockedPeeking = isDockedPeeking,
                        onHubStateChanged = { expanded ->
                            isHubOpen = expanded
                            if (expanded) {
                                cancelAutoHideTimer()
                                unhidePetFromEdge(immediately = false)
                            } else {
                                scheduleAutoHideTimer()
                            }
                        },
                        onDragStart = { rawX, rawY ->
                            cancelAutoHideTimer()
                            glideAnimator?.cancel()
                            initialWindowX = windowLayoutParams.x
                            initialWindowY = windowLayoutParams.y
                            initialTouchX = rawX
                            initialTouchY = rawY
                            isDraggingOverlay = false
                        },
                        onDragMove = { rawX, rawY ->
                            val deltaX = rawX - initialTouchX
                            val deltaY = rawY - initialTouchY
                            if (!isDraggingOverlay && Math.hypot(deltaX.toDouble(), deltaY.toDouble()) > 10.0) {
                                isDraggingOverlay = true
                                isDockedPeeking = false
                            }
                            if (isDraggingOverlay) {
                                windowLayoutParams.x = (initialWindowX + deltaX.roundToInt())
                                windowLayoutParams.y = (initialWindowY + deltaY.roundToInt()).coerceIn(40, getScreenHeight() - 160)
                                updateWindowLayout()
                            }
                        },
                        onDragEnd = {
                            val wasDragging = isDraggingOverlay
                            isDraggingOverlay = false
                            // Free floating: clamp to safe visible bounds on release
                            clampPositionToScreenBounds()
                            scheduleAutoHideTimer()
                            wasDragging
                        },
                        onPetTapped = {
                            if (isDockedPeeking) {
                                unhidePetFromEdge(immediately = false)
                            }
                            scheduleAutoHideTimer()
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
        scheduleAutoHideTimer()
    }

    private fun updateWindowLayout() {
        overlayComposeView?.let {
            try {
                windowManager.updateViewLayout(it, windowLayoutParams)
            } catch (_: Exception) {}
        }
    }

    private fun clampPositionToScreenBounds() {
        val screenWidth = getScreenWidth()
        val screenHeight = getScreenHeight()
        // Overlay view width/height is ~96dp (~260px depending on density), ensure full visibility
        windowLayoutParams.x = windowLayoutParams.x.coerceIn(8, screenWidth - 110)
        windowLayoutParams.y = windowLayoutParams.y.coerceIn(40, screenHeight - 160)
        updateWindowLayout()
    }

    /**
     * Resets and schedules an auto-dock timer.
     * When user has not interacted for 5 seconds and hub is closed,
     * smoothly glide to side edge of screen while staying 100% visible on screen.
     */
    private fun scheduleAutoHideTimer() {
        cancelAutoHideTimer()
        if (isHubOpen) return

        autoHideJob = serviceScope.launch {
            delay(AUTO_HIDE_IDLE_DELAY_MS)
            if (!isDraggingOverlay && !isHubOpen) {
                smoothGlideToEdgeAndPeek()
            }
        }
    }

    private fun cancelAutoHideTimer() {
        autoHideJob?.cancel()
        autoHideJob = null
    }

    /**
     * Smoothly glides the pet from its current screen position to the nearest side edge,
     * resting completely on screen (with comfortable 8px margin) so it is always fully visible.
     */
    private fun smoothGlideToEdgeAndPeek() {
        glideAnimator?.cancel()
        val screenWidth = getScreenWidth()
        val currentX = windowLayoutParams.x
        val midPoint = screenWidth / 2

        // Left vs Right target X coordinate: completely visible on screen
        val targetX = if (currentX + 50 < midPoint) {
            8 // Safe left margin, fully visible
        } else {
            (screenWidth - 104) // Safe right margin, fully visible
        }

        val startX = currentX
        glideAnimator = ValueAnimator.ofInt(startX, targetX).apply {
            duration = 420
            interpolator = DecelerateInterpolator(1.6f)
            addUpdateListener { anim ->
                windowLayoutParams.x = anim.animatedValue as Int
                updateWindowLayout()
            }
            start()
        }
        isDockedPeeking = true
    }

    /**
     * Smoothly restores the pet from docked side margin.
     */
    private fun unhidePetFromEdge(immediately: Boolean = false) {
        if (!isDockedPeeking) return
        isDockedPeeking = false
        glideAnimator?.cancel()

        val screenWidth = getScreenWidth()
        val currentX = windowLayoutParams.x
        val midPoint = screenWidth / 2

        val targetX = if (currentX + 50 < midPoint) {
            20
        } else {
            screenWidth - 116
        }

        if (immediately) {
            windowLayoutParams.x = targetX
            updateWindowLayout()
        } else {
            val startX = currentX
            glideAnimator = ValueAnimator.ofInt(startX, targetX).apply {
                duration = 220
                interpolator = DecelerateInterpolator(1.2f)
                addUpdateListener { anim ->
                    windowLayoutParams.x = anim.animatedValue as Int
                    updateWindowLayout()
                }
                start()
            }
        }
    }

    private fun setRoamMode(enable: Boolean) {
        roamJob?.cancel()
        if (enable) {
            cancelAutoHideTimer()
            roamJob = serviceScope.launch {
                while (isActive) {
                    delay(Random.nextLong(4000, 9000))
                    if (!isDraggingOverlay && !isHubOpen) {
                        val moveX = Random.nextInt(-45, 45)
                        val moveY = Random.nextInt(-45, 45)
                        windowLayoutParams.x = (windowLayoutParams.x + moveX).coerceIn(12, getScreenWidth() - 120)
                        windowLayoutParams.y = (windowLayoutParams.y + moveY).coerceIn(120, getScreenHeight() - 200)
                        updateWindowLayout()
                    }
                }
            }
        } else {
            scheduleAutoHideTimer()
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
        cancelAutoHideTimer()
        glideAnimator?.cancel()
        roamJob?.cancel()
        super.onDestroy()
        if (::repository.isInitialized) {
            repository.setOverlayActive(false)
        }
        serviceScope.cancel()
        lifecycleOwner.onPause()
        lifecycleOwner.onDestroy()
        overlayComposeView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
            overlayComposeView = null
        }
    }

    companion object {
        private const val CHANNEL_ID = "lumi_overlay_channel"
        private const val NOTIFICATION_ID = 2001
        private const val AUTO_HIDE_IDLE_DELAY_MS = 5000L
    }
}
