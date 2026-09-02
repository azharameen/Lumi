package com.example.core.utils

import android.content.Context
import android.util.Log
import com.example.data.local.LumiDatabase
import com.example.data.remote.ModelDownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Industrial-grade Storage Integrity Orchestrator.
 * Coordinates health checks across SQLite, DataStore, and Binary artifacts.
 * Performs self-healing (delete/reset) if corruption is detected.
 */
object IntegrityOrchestrator {

    private const val TAG = "IntegrityOrchestrator"

    suspend fun runFullIntegrityCheck(
        context: Context,
        database: LumiDatabase,
        modelManager: ModelDownloadManager
    ) = withContext(Dispatchers.IO) {
        Log.i(TAG, "Starting full storage integrity audit...")

        // 1. Database Health Check
        val isDbHealthy = database.performHealthCheck()
        if (!isDbHealthy) {
            Log.e(TAG, "Database corruption detected! Fallback to destructive migration will trigger on next access.")
        } else {
            Log.i(TAG, "SQLite Database: HEALTHY")
        }

        // 2. LLM Model Integrity (SHA-256 Verification)
        val corruptedModels = modelManager.verifyAllDownloadedModels()
        if (corruptedModels.isNotEmpty()) {
            Log.w(TAG, "Removed ${corruptedModels.size} corrupted models: ${corruptedModels.joinToString()}")
        } else {
            Log.i(TAG, "Local AI Models: HEALTHY")
        }

        // 3. Filesystem Sanity
        ensureStructuredFolders(context)

        Log.i(TAG, "Storage integrity audit complete.")
    }

    private fun ensureStructuredFolders(context: Context) {
        val folders = listOf("llm_models", "chat_attachments", "exports")
        folders.forEach { folderName ->
            val dir = java.io.File(context.filesDir, folderName)
            if (!dir.exists()) dir.mkdirs()
        }
    }
}
