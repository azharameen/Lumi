package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

import com.example.core.di.appModule
import com.example.core.utils.IntegrityOrchestrator
import com.example.data.local.LumiDatabase
import com.example.domain.connectors.IntegrationService
import com.example.domain.tools.CoreToolsModule
import com.example.domain.tools.IntegrationToolsModule
import com.example.framework.tools.SystemToolSuite
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LumiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Start Koin immediately as it's needed for many components
        if (org.koin.core.context.GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger()
                androidContext(this@LumiApplication)
                modules(appModule)
            }
        }
        
        // Offload all other heavy services to background
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            setupAppCheckDebugToken()
            initializeFirebase()
            
            delay(1200) // Wait for UI to stabilize

            val koin = org.koin.core.context.GlobalContext.get()
            val database: LumiDatabase = koin.get()
            val modelManager = com.example.data.remote.ModelDownloadManager.getInstance(this@LumiApplication)

            IntegrityOrchestrator.runFullIntegrityCheck(this@LumiApplication, database, modelManager)

            registerTools()
            createNotificationChannels()
        }
    }

    private suspend fun registerTools() {
        // Access Koin on background thread
        val koin = org.koin.core.context.GlobalContext.get()
        val taskGoalRepository: com.example.domain.repository.TaskGoalRepository = koin.get()
        val wellnessRepository: com.example.domain.repository.WellnessRepository = koin.get()
        val integrationService: IntegrationService = koin.get()
        
        SystemToolSuite.registerAll(this)
        CoreToolsModule.register(taskGoalRepository, wellnessRepository, integrationService)
        IntegrationToolsModule.register(integrationService)

        // Initialize and sync SQLite FTS5 fast tool search index
        try {
            val toolRetriever: com.example.domain.tools.ToolRetriever = koin.get()
            toolRetriever.initializeIndex()
        } catch (_: Exception) {}
    }

    private fun getApiKey(): String {
        return try {
            val field = BuildConfig::class.java.getDeclaredField("FIREBASE_API_KEY")
            field.get(null) as? String ?: ""
        } catch (e: Exception) {
            "" // Fallback if missing, though Firebase may fail to initialize properly if so.
        }
    }

    private fun initializeFirebase() {
        try {
            var isFirebaseInitialized = false
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                val app = com.google.firebase.FirebaseApp.initializeApp(this)
                if (app == null) {
                    val apiKey = getApiKey()
                    if (apiKey.isNotEmpty()) {
                        val options = com.google.firebase.FirebaseOptions.Builder()
                            .setApplicationId("1:663377968514:android:bac0ab54e860f4ed40639b")
                            .setApiKey(apiKey)
                            .setProjectId("studio-8325749739-eefac")
                            .setDatabaseUrl("https://studio-8325749739-eefac-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .setStorageBucket("studio-8325749739-eefac.firebasestorage.app")
                            .setGcmSenderId("663377968514")
                            .build()
                        com.google.firebase.FirebaseApp.initializeApp(this, options)
                        android.util.Log.i("LumiApp", "Firebase initialized with explicit fallback options")
                        isFirebaseInitialized = true
                    } else {
                        android.util.Log.w("LumiApp", "Firebase not initialized automatically and no API key found. Firebase features will be disabled.")
                    }
                } else {
                    android.util.Log.i("LumiApp", "Firebase initialized automatically from string resources")
                    isFirebaseInitialized = true
                }
            } else {
                android.util.Log.i("LumiApp", "Firebase initialized automatically from google-services")
                isFirebaseInitialized = true
            }
            
            if (isFirebaseInitialized) {
                // Initialize Firebase App Check with Play Integrity provider
                com.example.data.firebase.LumiAppCheckManager.getInstance().initialize()
                setupFcmSkipping()
            }
        } catch (e: Exception) {
            android.util.Log.e("LumiApp", "Error during Firebase initialization", e)
        }
    }

    private fun setupAppCheckDebugToken() {
        if (BuildConfig.DEBUG) {
            try {
                // Read via reflection to avoid compilation errors if not generated by secrets plugin
                val field = BuildConfig::class.java.getDeclaredField("FIREBASE_APPCHECK_DEBUG_TOKEN")
                val debugToken = field.get(null) as? String
                if (!debugToken.isNullOrEmpty() && debugToken != "none") {
                    Log.i("LumiApp", "Setting Firebase App Check debug token from BuildConfig")
                    System.setProperty("debug.firebase.appcheck.token", debugToken)
                } else {
                    Log.w("LumiApp", "App Check debug token is empty or not provided.")
                }
            } catch (e: NoSuchFieldException) {
                Log.w("LumiApp", "FIREBASE_APPCHECK_DEBUG_TOKEN not found in BuildConfig. Provide it in .env if needed.")
            } catch (e: Exception) {
                Log.e("LumiApp", "Failed to set App Check debug token", e)
            }
        }
    }

    private fun setupFcmSkipping() {
        val isEmulator = (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.HARDWARE.contains("goldfish")
                || android.os.Build.HARDWARE.contains("ranchu")
                || android.os.Build.HARDWARE.contains("cutf_cvm")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || android.os.Build.PRODUCT.contains("sdk_google")
                || android.os.Build.PRODUCT.contains("google_sdk")
                || android.os.Build.PRODUCT.contains("sdk")
                || android.os.Build.PRODUCT.contains("sdk_x86")
                || android.os.Build.PRODUCT.contains("vbox86p")
                || android.os.Build.PRODUCT.contains("emulator")
                || android.os.Build.PRODUCT.contains("simulator")

        if (com.example.BuildConfig.DEBUG && isEmulator) {
            Log.d("LumiApp", "Running on emulator, skipping FCM programatic interactions.")
        }
    }

    private fun createNotificationChannels() {
            val channel = NotificationChannel(
                "lumi_companion_service",
                "Lumi Companion Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows floating Lumi pet companion overlay status"
            }

            val reminderChannel = NotificationChannel(
                "lumi_reminders",
                "Lumi Daily Check-Ins",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily wellness, mindfulness and schedule reminders from Lumi"
            }

            val fcmChannel = NotificationChannel(
                com.example.framework.LumiFirebaseMessagingService.CHANNEL_PROACTIVE_ALERTS,
                "Lumi Proactive Companion Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Proactive check-ins, mindful nudges, and real-time alerts from your companion Lumi"
                enableVibration(true)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            manager.createNotificationChannel(reminderChannel)
            manager.createNotificationChannel(fcmChannel)
        // Subscribe to companion notification topics
        val isEmulator = (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.HARDWARE.contains("goldfish")
                || android.os.Build.HARDWARE.contains("ranchu")
                || android.os.Build.HARDWARE.contains("cutf_cvm")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.PRODUCT.contains("sdk")
                || android.os.Build.PRODUCT.contains("emulator")
                || android.os.Build.PRODUCT.contains("simulator")
                
        if (!isEmulator) {
            com.example.framework.LumiFirebaseMessagingService.subscribeToCompanionTopics()
        }
    }
}
