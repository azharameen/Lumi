package com.example.data.device

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ZenModeStatus(
    val isDndActive: Boolean = false,
    val modeName: String = "Normal"
)

/**
 * Monitors Android Do Not Disturb (DND) and Interruption Filter changes.
 * When DND is active, Lumi automatically goes into Quiet/Zen mode and softens speech volume.
 */
class ZenModeManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    private val _zenStatus = MutableStateFlow(getCurrentStatus())
    val zenStatus: StateFlow<ZenModeStatus> = _zenStatus.asStateFlow()

    private var onZenChanged: ((ZenModeStatus) -> Unit)? = null

    private val dndReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateStatus()
        }
    }

    fun startListening(onChanged: ((ZenModeStatus) -> Unit)? = null) {
        this.onZenChanged = onChanged
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val filter = IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            try {
                context.registerReceiver(dndReceiver, filter)
            } catch (e: Exception) {
                // Ignored
            }
        }
        updateStatus()
    }

    fun stopListening() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                context.unregisterReceiver(dndReceiver)
            } catch (e: Exception) {
                // Ignored
            }
        }
    }

    private fun updateStatus() {
        val status = getCurrentStatus()
        _zenStatus.value = status
        onZenChanged?.invoke(status)
    }

    private fun getCurrentStatus(): ZenModeStatus {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val filter = notificationManager?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
            return when (filter) {
                NotificationManager.INTERRUPTION_FILTER_NONE -> ZenModeStatus(true, "Total Silence")
                NotificationManager.INTERRUPTION_FILTER_ALARMS -> ZenModeStatus(true, "Alarms Only")
                NotificationManager.INTERRUPTION_FILTER_PRIORITY -> ZenModeStatus(true, "Priority Only")
                else -> ZenModeStatus(false, "Normal Mode")
            }
        }
        return ZenModeStatus(false, "Normal Mode")
    }
}
