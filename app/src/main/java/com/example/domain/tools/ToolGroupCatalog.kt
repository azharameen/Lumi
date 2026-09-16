package com.example.domain.tools

/**
 * Centralized catalog of logical tool groups.
 * Groups are defined by tool ID patterns so they don't need to be declared on each tool class.
 * New tools are automatically assigned to groups if their ID matches a prefix.
 */
object ToolGroupCatalog {

    data class GroupDef(
        val groupId: String,
        val displayName: String,
        val description: String,
        val category: ToolCategory,
        /** Tool ID prefixes or exact IDs that belong to this group. */
        val idPatterns: List<String>
    )

    val groups: List<GroupDef> = listOf(
        // ── System & Device ──────────────────────────────────
        GroupDef(
            groupId = "device_sensors",
            displayName = "Device Sensors",
            description = "Battery, storage, RAM, uptime, network status",
            category = ToolCategory.SYSTEM,
            idPatterns = listOf("device_get_battery", "device_get_storage", "device_get_ram", "device_get_uptime", "network_get_status")
        ),
        GroupDef(
            groupId = "device_controls",
            displayName = "Device Controls",
            description = "Flashlight, volume, ringer, haptics, brightness, power save",
            category = ToolCategory.SYSTEM,
            idPatterns = listOf("system_toggle_flashlight", "system_set_media_volume", "system_set_ringer", "system_set_alarm_volume", "system_set_ringer_mode", "device_trigger_haptic", "system_set_brightness", "toggle_power_save", "set_screen_timeout")
        ),
        GroupDef(
            groupId = "app_management",
            displayName = "App & Settings",
            description = "Open apps, launch system settings pages",
            category = ToolCategory.SYSTEM,
            idPatterns = listOf("system_open_app", "system_open_location_settings", "system_open_display_settings", "system_open_wifi_settings", "system_open_bluetooth_settings")
        ),
        GroupDef(
            groupId = "notifications",
            displayName = "Notifications",
            description = "Post, manage, and check notifications",
            category = ToolCategory.SYSTEM,
            idPatterns = listOf("notification_post_pet_alert", "get_firebase_notifications")
        ),
        GroupDef(
            groupId = "connectivity",
            displayName = "Connectivity",
            description = "Wi-Fi, Bluetooth, airplane mode, network control",
            category = ToolCategory.SYSTEM,
            idPatterns = listOf("set_wifi_on_off", "set_bt_on_off", "get_airplane_mode_status")
        ),

        // ── Audio & Media ────────────────────────────────────
        GroupDef(
            groupId = "media_playback",
            displayName = "Media Playback",
            description = "Play, pause, skip media tracks",
            category = ToolCategory.AUDIO,
            idPatterns = listOf("media_play_pause", "media_next_track", "media_previous_track")
        ),
        GroupDef(
            groupId = "voice_control",
            displayName = "Voice & TTS",
            description = "Voice listening, TTS, audio reactive mode",
            category = ToolCategory.AUDIO,
            idPatterns = listOf("voice_memo_record", "text_to_speech_article")
        ),

        // ── Time & Scheduling ────────────────────────────────
        GroupDef(
            groupId = "time_datetime",
            displayName = "Date & Time",
            description = "Current time, date, relative time, timezone",
            category = ToolCategory.TIME,
            idPatterns = listOf("get_current_datetime", "get_relative_time")
        ),
        GroupDef(
            groupId = "time_alarms",
            displayName = "Alarms & Timers",
            description = "Set alarms, timers, DND mode",
            category = ToolCategory.TIME,
            idPatterns = listOf("system_set_quick_timer", "system_set_alarm_clock", "system_get_dnd_status", "set_do_not_disturb", "set_zen_mode_timer")
        ),
        GroupDef(
            groupId = "time_reminders",
            displayName = "Reminders & Focus",
            description = "Smart reminders, pomodoro, focus sessions",
            category = ToolCategory.TIME,
            idPatterns = listOf("set_reminder", "pomodoro_start", "start_breathing_exercise", "start_mindfulness_meditation")
        ),
        GroupDef(
            groupId = "calendar_schedule",
            displayName = "Calendar & Schedule",
            description = "Calendar events, daily/weekly schedule",
            category = ToolCategory.CALENDAR,
            idPatterns = listOf("add_calendar_event", "get_daily_schedule", "get_weekly_schedule", "google_calendar_sync")
        ),
        GroupDef(
            groupId = "tasks_productivity",
            displayName = "Tasks & Productivity",
            description = "Create, complete, list, delete tasks",
            category = ToolCategory.CALENDAR,
            idPatterns = listOf("create_task", "complete_task", "list_pending_tasks", "delete_task")
        ),

        // ── Health & Wellness ────────────────────────────────
        GroupDef(
            groupId = "health_metrics",
            displayName = "Health Metrics",
            description = "Steps, heart rate, sleep, calories from Health Connect",
            category = ToolCategory.HEALTH,
            idPatterns = listOf("get_today_steps", "get_heart_rate", "get_sleep_summary", "get_calories_burned")
        ),
        GroupDef(
            groupId = "health_wellness",
            displayName = "Wellness & Mood",
            description = "Mood logging, hydration, breathing, food intake",
            category = ToolCategory.HEALTH,
            idPatterns = listOf("log_wellness", "log_food_intake", "streak_tracker")
        ),

        // ── Communication ────────────────────────────────────
        GroupDef(
            groupId = "comms_messaging",
            displayName = "Messaging",
            description = "SMS, WhatsApp, Slack, email",
            category = ToolCategory.COMMUNICATION,
            idPatterns = listOf("communication_draft_sms", "send_whatsapp_message", "slack_post_message", "google_send_email")
        ),
        GroupDef(
            groupId = "comms_phone",
            displayName = "Phone & Contacts",
            description = "Dial, contacts search, call",
            category = ToolCategory.COMMUNICATION,
            idPatterns = listOf("communication_dial_number", "get_contacts")
        ),

        // ── Location & Navigation ────────────────────────────
        GroupDef(
            groupId = "location_gps",
            displayName = "Location & GPS",
            description = "Current location, geofencing, nearest places",
            category = ToolCategory.LOCATION,
            idPatterns = listOf("system_get_current_location", "get_nearest_places")
        ),
        GroupDef(
            groupId = "location_nav",
            displayName = "Navigation",
            description = "Driving mode, navigation, maps",
            category = ToolCategory.LOCATION,
            idPatterns = listOf("start_driving_mode")
        ),

        // ── Knowledge & Search ───────────────────────────────
        GroupDef(
            groupId = "knowledge_search",
            displayName = "Web Search & Weather",
            description = "Web search, weather, local news",
            category = ToolCategory.KNOWLEDGE,
            idPatterns = listOf("web_search", "get_weather", "get_local_news")
        ),
        GroupDef(
            groupId = "knowledge_notes",
            displayName = "Notes & Journal",
            description = "Save, retrieve, and search notes",
            category = ToolCategory.PRODUCTIVITY,
            idPatterns = listOf("save_note", "get_saved_notes")
        ),
        GroupDef(
            groupId = "knowledge_clipboard",
            displayName = "Clipboard & Share",
            description = "Copy to clipboard, share content",
            category = ToolCategory.PRODUCTIVITY,
            idPatterns = listOf("copy_to_clipboard", "share_content")
        ),
        GroupDef(
            groupId = "knowledge_docs",
            displayName = "Document Processing",
            description = "Summarize, translate, read documents",
            category = ToolCategory.LEARNING,
            idPatterns = listOf("summarize_document", "translation_tool")
        ),

        // ── Finance ──────────────────────────────────────────
        GroupDef(
            groupId = "finance_expenses",
            displayName = "Expenses & Budget",
            description = "Log expenses, spending summaries, budgets",
            category = ToolCategory.FINANCE,
            idPatterns = listOf("log_expense", "get_spending_summary")
        ),
        GroupDef(
            groupId = "finance_payments",
            displayName = "Payments",
            description = "Payment links, currency conversion",
            category = ToolCategory.FINANCE,
            idPatterns = listOf("stripe_payment_link", "currency_convert")
        ),
        GroupDef(
            groupId = "finance_calculator",
            displayName = "Calculator",
            description = "Math, unit conversions, tips",
            category = ToolCategory.FINANCE,
            idPatterns = listOf("calculate")
        ),

        // ── Vision ───────────────────────────────────────────
        GroupDef(
            groupId = "vision_camera",
            displayName = "Camera & Vision",
            description = "Analyze images, take photos, OCR",
            category = ToolCategory.VISION,
            idPatterns = listOf("analyze_image", "take_photo_and_save")
        ),

        // ── Cloud Connectors ─────────────────────────────────
        GroupDef(
            groupId = "connector_google",
            displayName = "Google Workspace",
            description = "Gmail, Docs, Calendar, Drive",
            category = ToolCategory.CONNECTORS,
            idPatterns = listOf("google_send_email", "google_create_doc", "google_calendar_sync")
        ),
        GroupDef(
            groupId = "connector_dev",
            displayName = "Developer Tools",
            description = "GitHub, Notion, code-related",
            category = ToolCategory.CONNECTORS,
            idPatterns = listOf("github_create_issue", "notion_add_page")
        ),
        GroupDef(
            groupId = "connector_music",
            displayName = "Music & Media",
            description = "Spotify, streaming services",
            category = ToolCategory.CONNECTORS,
            idPatterns = listOf("spotify_control")
        ),

        // ── Smart Home & IoT ─────────────────────────────────
        GroupDef(
            groupId = "iot_smart_home",
            displayName = "Smart Home",
            description = "Lights, thermostat, plugs via Home Assistant",
            category = ToolCategory.IOT,
            idPatterns = listOf("home_assistant_command")
        ),
        GroupDef(
            groupId = "iot_bluetooth",
            displayName = "Bluetooth & NFC",
            description = "Bluetooth devices, NFC tags",
            category = ToolCategory.IOT,
            idPatterns = listOf("bluetooth_device_control", "nfc_tag_interact")
        ),

        // ── Learning ─────────────────────────────────────────
        GroupDef(
            groupId = "learning_flashcards",
            displayName = "Flashcards & Quiz",
            description = "Spaced repetition, quizzes",
            category = ToolCategory.LEARNING,
            idPatterns = listOf("flashcard_quiz")
        ),

        // ── Proactive ────────────────────────────────────────
        GroupDef(
            groupId = "proactive_ambient",
            displayName = "Proactive & Ambient",
            description = "Auto check-ins, predictive routines, adaptive soundscapes",
            category = ToolCategory.PROACTIVE,
            idPatterns = listOf("proactive_checkin", "predictive_routine", "ambient_soundscape_auto", "get_daily_briefing")
        ),
        GroupDef(
            groupId = "proactive_pet",
            displayName = "Pet & Fun",
            description = "Pet evolution, jokes, random choices",
            category = ToolCategory.PROACTIVE,
            idPatterns = listOf("pet_evolution_trigger", "tell_joke_or_fact", "random_choice")
        ),

        // ── Security ─────────────────────────────────────────
        GroupDef(
            groupId = "security_biometric",
            displayName = "Biometrics & Security",
            description = "Fingerprint, face auth, vault access",
            category = ToolCategory.SECURITY,
            idPatterns = listOf("system_check_biometrics")
        ),

        // ── Microsoft Learn MCP ──────────────────────────────
        GroupDef(
            groupId = "mcp_microsoft_learn",
            displayName = "Microsoft Learn & Docs",
            description = "Search and fetch Microsoft documentation via MCP",
            category = ToolCategory.LEARN,
            idPatterns = listOf("mcp_microsoft_learn_")
        ),
        GroupDef(
            groupId = "mcp_generic",
            displayName = "MCP Remote Tools",
            description = "Remote MCP server tools",
            category = ToolCategory.CONNECTORS,
            idPatterns = listOf("mcp_")
        )
    )

    /**
     * Resolves which group a tool belongs to based on its ID.
     * Returns the first matching group, or null if ungrouped.
     */
    fun resolveGroupForTool(toolId: String): GroupDef? {
        // Exact match first
        for (group in groups) {
            if (group.idPatterns.contains(toolId)) {
                return group
            }
        }
        // Prefix match (for dynamic tools like MCP)
        for (group in groups) {
            if (group.idPatterns.any { pattern -> toolId.startsWith(pattern) }) {
                return group
            }
        }
        return null
    }
}
