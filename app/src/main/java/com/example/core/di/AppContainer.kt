package com.example.core.di

import android.content.Context
import com.example.domain.repository.LumiRepository
import com.example.data.repository.LumiRepositoryImpl
import com.example.domain.account.UserProfileManager
import com.example.data.device.*
import com.example.data.remote.ModelDownloadManager
import com.example.domain.briefing.AutonomousBriefingEngine

interface AppContainer {
    val repository: LumiRepository
    val userProfileManager: UserProfileManager
    val voiceEngine: VoiceEngine
    val sensorsManager: SensorsManager
    val batteryManager: BatteryStatusManager
    val locationEngine: ContextLocationEngine
    val clipboardAssistant: ClipboardAssistant
    val audioReactiveEngine: RealtimeAudioReactiveEngine
    val networkEngine: NetworkConnectivityEngine
    val headsetManager: AudioHeadsetManager
    val zenManager: ZenModeManager
    val biometricVault: BiometricVaultManager
    val modelDownloadManager: ModelDownloadManager
    val briefingEngine: AutonomousBriefingEngine
    val healthConnectManager: HealthConnectManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val repository: LumiRepository by lazy {
        LumiRepositoryImpl.getInstance(context, healthConnectManager)
    }
    
    override val userProfileManager: UserProfileManager by lazy {
        UserProfileManager.getInstance(context)
    }
    
    override val voiceEngine: VoiceEngine by lazy { VoiceEngine(context) }
    override val sensorsManager: SensorsManager by lazy { SensorsManager(context) }
    override val batteryManager: BatteryStatusManager by lazy { BatteryStatusManager(context) }
    override val locationEngine: ContextLocationEngine by lazy { ContextLocationEngine(context) }
    override val clipboardAssistant: ClipboardAssistant by lazy { ClipboardAssistant(context) }
    override val audioReactiveEngine: RealtimeAudioReactiveEngine by lazy { RealtimeAudioReactiveEngine(context) }
    override val networkEngine: NetworkConnectivityEngine by lazy { NetworkConnectivityEngine(context) }
    override val headsetManager: AudioHeadsetManager by lazy { AudioHeadsetManager(context) }
    override val zenManager: ZenModeManager by lazy { ZenModeManager(context) }
    override val biometricVault: BiometricVaultManager by lazy { BiometricVaultManager(context) }
    
    override val modelDownloadManager: ModelDownloadManager by lazy { ModelDownloadManager.getInstance(context) }
    override val briefingEngine: AutonomousBriefingEngine by lazy { AutonomousBriefingEngine(context) }
    override val healthConnectManager: HealthConnectManager by lazy { HealthConnectManager(context) }
}
