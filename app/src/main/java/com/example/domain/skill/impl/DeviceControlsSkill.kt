package com.example.domain.skill.impl

import com.example.data.remote.GeminiFunctionDeclaration
import com.example.data.remote.GeminiParametersSchema
import com.example.data.remote.GeminiPropertySchema
import com.example.data.remote.GeminiToolWrapper
import com.example.domain.skill.AgentSkill

class DeviceControlsSkill : AgentSkill {
    override val id: String = "DEVICE_CONTROLS"
    override val displayName: String = "Device Hardware & Settings"
    override val description: String = "Direct control over Android device settings, flashlight, bluetooth, wifi, volume, alarms, and timers."
    override val systemPromptExtension: String = "Focus: Controlling device features like flashlight, volume, bluetooth, wifi, and opening apps or alarms when requested."
    override val isTransactional: Boolean = true

    override val tools: List<GeminiToolWrapper> = listOf(
        GeminiToolWrapper(
            functionDeclarations = listOf(
                GeminiFunctionDeclaration(
                    name = "system_toggle_flashlight",
                    description = "Turns camera flashlight / torch on or off",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "state" to GeminiPropertySchema(type = "BOOLEAN", description = "true to turn ON flashlight, false to turn OFF")
                        ),
                        required = listOf("state")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "system_open_bluetooth_settings",
                    description = "Opens the Android system Bluetooth settings page",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "system_open_wifi_settings",
                    description = "Opens the Android system Wi-Fi settings page",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "system_open_location_settings",
                    description = "Opens the Android system GPS and Location settings page",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "system_open_app",
                    description = "Launches an installed Android app by name (e.g. Spotify, YouTube, WhatsApp, Camera)",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "appName" to GeminiPropertySchema(type = "STRING", description = "Name of application to launch")
                        ),
                        required = listOf("appName")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "system_set_media_volume",
                    description = "Sets media volume percentage from 0 to 100",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "level" to GeminiPropertySchema(type = "INTEGER", description = "Volume level percentage between 0 and 100")
                        ),
                        required = listOf("level")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "system_battery_status",
                    description = "Checks current device battery percentage and charging state",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "set_timer",
                    description = "Sets a countdown timer on the device",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "seconds" to GeminiPropertySchema(type = "INTEGER", description = "Timer duration in seconds"),
                            "label" to GeminiPropertySchema(type = "STRING", description = "Optional label for the timer")
                        ),
                        required = listOf("seconds")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "system_open_display_settings",
                    description = "Opens the Android system display and brightness settings page",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "system_set_ringer_volume",
                    description = "Sets device ringer and notification volume level percentage from 0 to 100",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "level" to GeminiPropertySchema(type = "INTEGER", description = "Ringer volume level percentage between 0 and 100")
                        ),
                        required = listOf("level")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "system_storage_info",
                    description = "Queries available internal disk storage space on the device",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "system_ram_usage",
                    description = "Queries available and total device RAM memory usage",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "system_device_uptime",
                    description = "Checks how long the device has been awake and running since last boot",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "system_network_status",
                    description = "Checks active Wi-Fi and Cellular network connectivity status",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "set_alarm",
                    description = "Sets an alarm clock on the device",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "hour" to GeminiPropertySchema(type = "INTEGER", description = "Hour of the day (0-23)"),
                            "minute" to GeminiPropertySchema(type = "INTEGER", description = "Minute of the hour (0-59)"),
                            "message" to GeminiPropertySchema(type = "STRING", description = "Alarm label message")
                        ),
                        required = listOf("hour", "minute")
                    )
                )
            )
        )
    )
}
