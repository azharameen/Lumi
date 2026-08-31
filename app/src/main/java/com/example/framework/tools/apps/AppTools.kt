package com.example.framework.tools.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import com.example.domain.tools.*

object AppToolsModule {
    fun register(context: Context, registry: ToolRegistry = ToolRegistry.getInstance()) {
        registry.registerTool(OpenInstalledAppTool(context))
        registry.registerTool(OpenLocationSettingsTool(context))
        registry.registerTool(OpenDisplaySettingsTool(context))
        registry.registerTool(OpenWifiSettingsTool(context))
        registry.registerTool(OpenBluetoothSettingsTool(context))
    }
}

class OpenInstalledAppTool(private val context: Context) : LumiTool {
    override val id = "system_open_app"
    override val displayName = "Open Application"
    override val description = "Launches an installed Android app by name (e.g. Spotify, YouTube, WhatsApp, Camera)"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.MEDIUM
    override val parameters = listOf(
        ToolParameter("appName", "string", "Name of application to launch", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val targetName = params["appName"]?.toString()?.lowercase() ?: ""
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            
            val matchedApp = packages.find { app ->
                val label = pm.getApplicationLabel(app).toString().lowercase()
                label.contains(targetName) || app.packageName.lowercase().contains(targetName)
            }

            if (matchedApp != null) {
                val launchIntent = pm.getLaunchIntentForPackage(matchedApp.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    val appLabel = pm.getApplicationLabel(matchedApp)
                    ToolExecutionResult(true, "Launched $appLabel (${matchedApp.packageName})")
                } else {
                    ToolExecutionResult(false, "App has no launchable main activity")
                }
            } else {
                ToolExecutionResult(false, "No app matching '$targetName' found on device")
            }
        } catch (e: Exception) {
            ToolExecutionResult(false, "App launch error: ${e.localizedMessage}")
        }
    }
}

class OpenLocationSettingsTool(private val context: Context) : LumiTool {
    override val id = "system_open_location_settings"
    override val displayName = "Open Location Settings"
    override val description = "Launches Android system GPS & Location settings page"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(true, "Opened Location settings")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Failed to open Location settings: ${e.localizedMessage}")
        }
    }
}

class OpenDisplaySettingsTool(private val context: Context) : LumiTool {
    override val id = "system_open_display_settings"
    override val displayName = "Open Display Settings"
    override val description = "Launches Android system Display & Screen settings page"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(true, "Opened Display settings")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Failed to open Display settings: ${e.localizedMessage}")
        }
    }
}

class OpenWifiSettingsTool(private val context: Context) : LumiTool {
    override val id = "system_open_wifi_settings"
    override val displayName = "Open Wi-Fi Settings"
    override val description = "Launches Android system Wi-Fi settings page"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(true, "Opened Wi-Fi settings page")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Failed to open Wi-Fi settings: ${e.localizedMessage}")
        }
    }
}

class OpenBluetoothSettingsTool(private val context: Context) : LumiTool {
    override val id = "system_open_bluetooth_settings"
    override val displayName = "Open Bluetooth Settings"
    override val description = "Launches Android system Bluetooth settings page"
    override val category = ToolCategory.SYSTEM
    override val riskLevel = ToolRiskLevel.LOW
    override val parameters = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(true, "Opened Bluetooth settings page")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Failed to open Bluetooth settings: ${e.localizedMessage}")
        }
    }
}
