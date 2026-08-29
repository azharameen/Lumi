package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatteryStatus(
    val levelPercent: Int = 100,
    val isCharging: Boolean = false,
    val isLow: Boolean = false,
    val isPowerSaveMode: Boolean = false
)

/**
 * System battery and power listener that provides reactive battery health & charging state
 * so Lumi can dynamically react (e.g. yawn on low power, spark lightning auras when charging).
 */
class BatteryStatusManager(private val context: Context) {

    private val _batteryStatus = MutableStateFlow(BatteryStatus())
    val batteryStatus: StateFlow<BatteryStatus> = _batteryStatus.asStateFlow()

    private var onBatteryChanged: ((BatteryStatus) -> Unit)? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { updateFromIntent(it) }
        }
    }

    fun startListening(onChanged: ((BatteryStatus) -> Unit)? = null) {
        this.onBatteryChanged = onChanged
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
        }
        val stickyIntent = context.registerReceiver(batteryReceiver, filter)
        stickyIntent?.let { updateFromIntent(it) }
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }

    private fun updateFromIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        val batteryPct = if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else 100

        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val isLow = batteryPct <= 20

        val newStatus = BatteryStatus(
            levelPercent = batteryPct,
            isCharging = isCharging,
            isLow = isLow
        )
        _batteryStatus.value = newStatus
        onBatteryChanged?.invoke(newStatus)
    }
}
