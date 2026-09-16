package com.example.framework.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.domain.tools.ToolPermissionChecker
import java.util.Locale

class AndroidToolPermissionChecker(
    private val context: Context
) : ToolPermissionChecker {

    override fun hasPermissionForTool(toolId: String): Pair<Boolean, String?> {
        val requiredPermissions = getRequiredPermissions(toolId)
        if (requiredPermissions.isEmpty()) {
            return true to null
        }

        val missingPermission = requiredPermissions.find { perm ->
            ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED
        }

        return if (missingPermission == null) {
            true to null
        } else {
            false to missingPermission
        }
    }

    private fun getRequiredPermissions(toolId: String): List<String> {
        val id = toolId.lowercase(Locale.ROOT)
        return when {
            id.contains("location") || id.contains("gps") -> listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            id.contains("camera") || id.contains("photo") -> listOf(Manifest.permission.CAMERA)
            id.contains("record") || id.contains("audio") || id.contains("mic") -> listOf(Manifest.permission.RECORD_AUDIO)
            id.contains("sms") -> listOf(Manifest.permission.SEND_SMS)
            id.contains("contacts") -> listOf(Manifest.permission.READ_CONTACTS)
            id.contains("calendar") -> listOf(Manifest.permission.READ_CALENDAR)
            else -> emptyList()
        }
    }
}
