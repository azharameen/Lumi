package com.example.core.di

import com.example.data.device.*
import com.example.data.firebase.*
import com.example.data.local.LumiDatabase
import com.example.data.remote.HybridAiEngine
import com.example.data.remote.LocalVisionEngine
import com.example.data.remote.ModelDownloadManager
import com.example.data.remote.OnDeviceGemmaEngine
import com.example.data.remote.google.GoogleWorkspaceAuthManager
import com.example.data.remote.google.GoogleWorkspaceRestEngine
import com.example.data.repository.*
import com.example.data.tools.FastToolIndex
import com.example.domain.account.UserProfileRepository
import com.example.domain.billing.TokenBillingManager
import com.example.domain.briefing.AutonomousBriefingEngine
import com.example.domain.connectors.ConnectorRepository
import com.example.domain.connectors.IntegrationService
import com.example.domain.mcp.McpClientEngine
import com.example.domain.mcp.McpJsonRpcClient
import com.example.domain.planner.AutonomousGoalPlanner
import com.example.domain.repository.*
import com.example.domain.tools.AgentToolDispatcher
import com.example.domain.tools.ToolRegistry
import com.example.domain.tools.ToolRetriever
import com.example.domain.usecase.chat.SendMessageUseCase
import com.example.domain.usecase.goal.DecomposeGoalUseCase
import com.example.domain.usecase.pet.PetInteractionUseCase
import com.example.presentation.viewmodel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database instance
    single { LumiDatabase.getDatabase(androidContext()) }

    // Coroutine Scopes
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    // Firebase Enterprise Infrastructure
    single { LumiCrashlyticsManager() }
    single { LumiAnalyticsManager(androidContext()) }
    single { LumiPerformanceManager() }
    single { LumiRemoteConfigManager() }
    single { LumiAppCheckManager.getInstance() }

    // Infrastructure & System Managers
    single { HealthConnectManager(androidContext()) }
    single<UserProfileRepository> { UserProfileRepositoryImpl(androidContext()) }
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
    single { TokenBillingManager(androidContext()) }

    // Google Workspace OAuth & REST Engine
    single { GoogleWorkspaceAuthManager(androidContext(), get()) }
    single { GoogleWorkspaceRestEngine(get()) }

    // MCP JSON-RPC Engine
    single { McpJsonRpcClient() }
    single { McpClientEngine(get()) }

    // 1000+ Tools & FTS Engine Singletons
    single { ToolRegistry.getInstance() }
    single { FastToolIndex(get<LumiDatabase>().toolFtsDao(), get()) }
    single { ToolRetriever(get(), get()) }
    
    // Connectors & Tool Dispatching
    single<ConnectorRepository> { ConnectorRepositoryImpl(androidContext()) }
    single { get<ConnectorRepository>() as ConnectorRepositoryImpl }
    single { IntegrationService(get<ConnectorRepository>(), get<GoogleWorkspaceRestEngine>()) }
    single { AgentToolDispatcher(get()) }

    // AI Engines
    single { OnDeviceGemmaEngine(get(), get(), androidContext(), get()) }
    single { LocalVisionEngine(androidContext()) }
    single { HybridAiEngine(get(), get<LumiDatabase>().aiExecutionLogDao(), get(), androidContext(), get(), get()) }
    single { AutonomousGoalPlanner(get(), get(), get()) }

    // Specialized Clean Domain Repositories
    single<PetRepository> { PetRepositoryImpl(get(), get()) }
    single<ChatRepository> { ChatRepositoryImpl(get(), get(), get()) }
    single<WellnessRepository> { WellnessRepositoryImpl(get(), get()) }
    single<TaskGoalRepository> { TaskGoalRepositoryImpl(get(), get(), get()) }
    single<PetMemoryRepository> { PetMemoryRepositoryImpl(get()) }

    // UseCases
    single { SendMessageUseCase(get()) }
    single { PetInteractionUseCase(get()) }
    single { DecomposeGoalUseCase(get()) }

    // Legacy/Facade Repository
    single<LumiRepository> { LumiRepositoryImpl.getInstance(androidContext(), get()) }

    // Other repositories
    single<AuthRepository> { FirebaseAuthRepositoryImpl(androidContext()) }
    single<PetCompanionRepository> { PetCompanionRepositoryImpl(get(), get()) }
    single<UserMemoryRepository> { UserMemoryRepositoryImpl(get(), get<LumiDatabase>().factKnowledgeDao(), get<LumiDatabase>().chatMessageDao(), get()) }

    // ViewModels
    viewModel { AuthViewModel(get(), get(), get(), get()) }
    viewModel { AiSettingsViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get(), get(), get()) }
    viewModel { LifeHubViewModel(get(), get(), get(), get(), get()) }
    viewModel { 
        LumiViewModel(
            petRepository = get(),
            chatRepository = get(),
            wellnessRepository = get(),
            taskGoalRepository = get(),
            sendMessageUseCase = get(),
            petInteractionUseCase = get(),
            userProfileManager = get(),
            voiceEngine = get(),
            sensorsManager = get(),
            batteryManager = get(),
            locationEngine = get(),
            clipboardAssistant = get(),
            audioReactiveEngine = get(),
            networkEngine = get(),
            headsetManager = get(),
            zenManager = get(),
            biometricVault = get(),
            briefingEngine = get()
        ) 
    }
    viewModel { PetViewModel(get(), get(), get(), get()) }
    viewModel { WellnessViewModel(get(), get(), get()) }
}
