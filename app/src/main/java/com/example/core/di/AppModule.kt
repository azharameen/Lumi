package com.example.core.di

import com.example.data.device.*
import com.example.data.remote.ModelDownloadManager
import com.example.data.repository.LumiRepositoryImpl
import com.example.domain.account.UserProfileManager
import com.example.domain.briefing.AutonomousBriefingEngine
import com.example.domain.repository.LumiRepository
import com.example.presentation.viewmodel.*
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { HealthConnectManager(androidContext()) }
    single<LumiRepository> { LumiRepositoryImpl.getInstance(androidContext(), get()) }
    single { UserProfileManager.getInstance(androidContext()) }
    single { VoiceEngine(androidContext()) }
    single { SensorsManager(androidContext()) }
    single { BatteryStatusManager(androidContext()) }
    single { ContextLocationEngine(androidContext()) }
    single { ClipboardAssistant(androidContext()) }
    single { RealtimeAudioReactiveEngine(androidContext()) }
    single { NetworkConnectivityEngine(androidContext()) }
    single { AudioHeadsetManager(androidContext()) }
    single { ZenModeManager(androidContext()) }
    single { BiometricVaultManager(androidContext()) }
    single { ModelDownloadManager.getInstance(androidContext()) }
    single { AutonomousBriefingEngine(androidContext()) }

    viewModel { AiSettingsViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get()) }
    viewModel { LifeHubViewModel(get(), get(), get()) }
    viewModel { LumiViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { PetViewModel(get(), get()) }
    viewModel { WellnessViewModel(get(), get()) }
}

