package com.example.data.device

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.example.domain.onboarding.DeviceCapabilityScanner
import com.example.domain.onboarding.DeviceProfile

class DeviceCapabilityScannerImpl(private val context: Context) : DeviceCapabilityScanner {

    override fun scanDevice(): DeviceProfile {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val stat = StatFs(Environment.getDataDirectory().path)
        val freeStorage = stat.availableBytes

        val isAiCore = try {
            context.packageManager.hasSystemFeature("android.hardware.telephony")
        } catch (_: Exception) {
            false
        }

        return DeviceProfile(
            totalRamBytes = memoryInfo.totalMem,
            availableRamBytes = memoryInfo.availMem,
            freeStorageBytes = freeStorage,
            isLowRamDevice = memoryInfo.lowMemory,
            apiLevel = Build.VERSION.SDK_INT,
            isAiCoreAvailable = isAiCore
        )
    }
}
