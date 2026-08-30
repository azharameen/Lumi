package com.example.framework

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R

/**
 * Manages Dynamic and Pinned Android Launcher App Shortcuts (long-press app icon on home screen).
 */
object AppShortcutsManager {

    fun initDynamicShortcuts(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        try {
            val voiceIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("SHORTCUT_ACTION", "VOICE_CHAT")
                putExtra("NAVIGATE_TAB", 1)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val waterIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("SHORTCUT_ACTION", "QUICK_WATER")
                putExtra("NAVIGATE_TAB", 2)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val breathIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("SHORTCUT_ACTION", "START_BREATHING")
                putExtra("NAVIGATE_TAB", 0)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val overlayIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("SHORTCUT_ACTION", "TOGGLE_OVERLAY")
                putExtra("NAVIGATE_TAB", 0)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val shortcuts = listOf(
                ShortcutInfoCompat.Builder(context, "dyn_shortcut_voice_chat")
                    .setShortLabel("Talk to Lumi")
                    .setLongLabel("Quick Voice Conversation")
                    .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(voiceIntent)
                    .setRank(1)
                    .build(),

                ShortcutInfoCompat.Builder(context, "dyn_shortcut_quick_water")
                    .setShortLabel("+1 Water")
                    .setLongLabel("Log Quick Hydration")
                    .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(waterIntent)
                    .setRank(2)
                    .build(),

                ShortcutInfoCompat.Builder(context, "dyn_shortcut_breathing")
                    .setShortLabel("Mindful Breath")
                    .setLongLabel("4-7-8 Coherence Session")
                    .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(breathIntent)
                    .setRank(3)
                    .build(),

                ShortcutInfoCompat.Builder(context, "dyn_shortcut_toggle_overlay")
                    .setShortLabel("Screen Companion")
                    .setLongLabel("Toggle Floating Lumi Pet")
                    .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(overlayIntent)
                    .setRank(4)
                    .build()
            )

            ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
        } catch (e: Exception) {
            android.util.Log.w("AppShortcutsManager", "Unable to set dynamic shortcuts: ${e.message}")
        }
    }
}
