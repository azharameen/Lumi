package com.example.framework.tools

import android.content.Context
import com.example.domain.tools.ToolRegistry
import com.example.framework.tools.apps.AppToolsModule
import com.example.framework.tools.audio.AudioToolsModule
import com.example.framework.tools.communication.CommunicationToolsModule
import com.example.framework.tools.media.MediaToolsModule
import com.example.framework.tools.system.SystemToolsModule
import com.example.framework.tools.time.TimeToolsModule

/**
 * Enterprise Orchestrator for Tool Module Registration.
 * Modularized by Domain Packages to prevent God File anti-patterns:
 * - audio/ (Flashlight, Volume, RingerMode)
 * - media/ (Media playback keys)
 * - system/ (Battery, Storage, RAM, Uptime, Haptics, Notifications, Network)
 * - apps/ (App launcher, Location/Display/WiFi/BT settings)
 * - time/ (Alarms, Timers, DND)
 * - communication/ (SMS, Phone Dialer)
 */
object SystemToolSuite {

    fun registerAll(context: Context, registry: ToolRegistry = ToolRegistry.getInstance()) {
        AudioToolsModule.register(context, registry)
        MediaToolsModule.register(context, registry)
        SystemToolsModule.register(context, registry)
        AppToolsModule.register(context, registry)
        TimeToolsModule.register(context, registry)
        CommunicationToolsModule.register(context, registry)
    }
}
