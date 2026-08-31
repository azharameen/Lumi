package com.example.core.di

import com.example.data.device.*
import com.example.data.local.LumiDatabase
import com.example.data.remote.ModelDownloadManager
import com.example.data.repository.*
import com.example.data.tools.FastToolIndex
import com.example.domain.account.UserProfileManager
import com.example.domain.briefing.AutonomousBriefingEngine
import com.example.domain.repository.*
import com.example.domain.tools.ToolRegistry
import com.example.domain.tools.ToolRetriever
import com.example.presentation.viewmodel.*
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Infrastructure & System Managers
    single { HealthConnectManager(androidContext()) }
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
    single { ProceduralSoundscapeEngine.getInstance(androidContext()) }

    // 1000+ Tools & FTS Engine Singletons
    single { ToolRegistry.getInstance() }
    single { FastToolIndex(LumiDatabase.getInstance(androidContext()).toolFtsDao(), get()) }
    single { ToolRetriever(get(), get()) }

    // Facade Repository
    single<LumiRepository> { LumiRepositoryImpl.getInstance(androidContext(), get()) }

    // Specialized Clean Domain Repositories
    single<PetCompanionRepository> { PetCompanionRepositoryImpl(get(), get()) }
    single<UserMemoryRepository> { UserMemoryRepositoryImpl(get(), LumiDatabase.getInstance(androidContext()).factKnowledgeDao(), LumiDatabase.getInstance(androidContext()).chatMessageDao(), get()) }
    single<TaskGoalRepository> { TaskGoalRepositoryImpl(LumiDatabase.getInstance(androidContext()).taskDao(), LumiDatabase.getInstance(androidContext()).calendarEventDao()) }

    // ViewModels
    viewModel { AiSettingsViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get()) }
    viewModel { LifeHubViewModel(get(), get(), get()) }
    viewModel { LumiViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { PetViewModel(get(), get()) }
    viewModel { WellnessViewModel(get(), get()) }
}
