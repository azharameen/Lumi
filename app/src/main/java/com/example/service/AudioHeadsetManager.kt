package com.example.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioHeadsetStatus(
    val isHeadsetConnected: Boolean = false,
    val deviceName: String = "Internal Speaker",
    val isBluetooth: Boolean = false
)

/**
 * Monitors wired headphone and Bluetooth audio connection status.
 */
class AudioHeadsetManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _headsetStatus = MutableStateFlow(getCurrentStatus())
    val headsetStatus: StateFlow<AudioHeadsetStatus> = _headsetStatus.asStateFlow()

    private var onHeadsetChanged: ((AudioHeadsetStatus) -> Unit)? = null

    private val headsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateStatus()
        }
    }

    fun startListening(onChanged: ((AudioHeadsetStatus) -> Unit)? = null) {
        this.onHeadsetChanged = onChanged
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        }
        try {
            context.registerReceiver(headsetReceiver, filter)
            updateStatus()
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(headsetReceiver)
        } catch (e: Exception) {
            // Ignored
        }
    }

    private fun updateStatus() {
        val status = getCurrentStatus()
        _headsetStatus.value = status
        onHeadsetChanged?.invoke(status)
    }

    private fun getCurrentStatus(): AudioHeadsetStatus {
        val am = audioManager ?: return AudioHeadsetStatus()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                when (device.type) {
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_USB_HEADSET -> {
                        return AudioHeadsetStatus(
                            isHeadsetConnected = true,
                            deviceName = "Wired Headphones",
                            isBluetooth = false
                        )
                    }
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_BLE_HEADSET -> {
                        return AudioHeadsetStatus(
                            isHeadsetConnected = true,
                            deviceName = "Bluetooth Audio",
                            isBluetooth = true
                        )
                    }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            if (am.isWiredHeadsetOn) {
                return AudioHeadsetStatus(
                    isHeadsetConnected = true,
                    deviceName = "Wired Headphones",
                    isBluetooth = false
                )
            }
            @Suppress("DEPRECATION")
            if (am.isBluetoothA2dpOn || am.isBluetoothScoOn) {
                return AudioHeadsetStatus(
                    isHeadsetConnected = true,
                    deviceName = "Bluetooth Audio",
                    isBluetooth = true
                )
            }
        }

        return AudioHeadsetStatus(
            isHeadsetConnected = false,
            deviceName = "Speaker",
            isBluetooth = false
        )
    }
}
