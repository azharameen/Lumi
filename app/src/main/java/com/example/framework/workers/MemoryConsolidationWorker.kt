package com.example.framework.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.data.local.LumiDatabase
import com.example.data.local.entity.FactKnowledgeEntity
import com.example.domain.memory.VectorEmbeddingUtils
import com.example.domain.memory.WordEmbeddingSimilarity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext
import java.util.concurrent.TimeUnit

/**
 * Sleep & Dream WorkManager Worker.
 * Executes during overnight charging/idle windows to:
 * 1. Consolidate episodic chat messages into long-term factual knowledge triples.
 * 2. Perform garbage collection / confidence decay on stale, unpinned knowledge facts.
 * 3. Precompute vector embeddings for newly added facts so runtime queries are instant.
 */
class MemoryConsolidationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "MemoryConsolidationWorker"
        const val WORK_NAME = "lumi_memory_consolidation_work"

        fun schedule(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiresCharging(true)
                    .build()

                val consolidationRequest = PeriodicWorkRequestBuilder<MemoryConsolidationWorker>(
                    24, TimeUnit.HOURS,
                    6, TimeUnit.HOURS
                )
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    consolidationRequest
                )
                Log.d(TAG, "MemoryConsolidationWorker scheduled successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule MemoryConsolidationWorker: ${e.message}", e)
            }
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(TAG, "Starting overnight memory consolidation & cognitive dreaming cycle...")
        try {
            val koin = GlobalContext.getOrNull() ?: return@withContext Result.retry()
            val database: LumiDatabase = koin.get()
            val factDao = database.factKnowledgeDao()
            val chatDao = database.chatMessageDao()

            val recentMessages = chatDao.getRecentMessagesDirect().take(50)
            val allFacts = factDao.getAllFactsDirect().toMutableList()

            for (msg in recentMessages) {
                if (msg.sender == "USER") {
                    val content = msg.content.lowercase(java.util.Locale.ROOT)
                    val prefixes = listOf("i prefer ", "i like ", "i love ", "my goal is ", "i am a ", "i work at ")
                    val matchedPrefix = prefixes.find { content.contains(it) }
                    if (matchedPrefix != null) {
                        val startIndex = content.indexOf(matchedPrefix) + matchedPrefix.length
                        val capturedValue = msg.content.substring(startIndex).take(50).trim().trimEnd('.', '!', ',')
                        val predicate = when (matchedPrefix.trim()) {
                            "i prefer" -> "preference"
                            "i like", "i love" -> "likes"
                            "my goal is" -> "goal"
                            "i am a" -> "identity"
                            "i work at" -> "workplace"
                            else -> "fact"
                        }

                        if (capturedValue.length in 3..60) {
                            val existing = allFacts.find { it.predicate.equals(predicate, ignoreCase = true) }
                            if (existing != null) {
                                if (existing.objectValue.equals(capturedValue, ignoreCase = true)) {
                                    factDao.insertOrUpdateFact(
                                        existing.copy(
                                            confidence = (existing.confidence + 0.05f).coerceAtMost(1.0f),
                                            accessCount = existing.accessCount + 1,
                                            lastUpdatedMillis = System.currentTimeMillis()
                                        )
                                    )
                                }
                            } else {
                                val vec = WordEmbeddingSimilarity.getEmbedding("$predicate $capturedValue")
                                val blob = if (vec.isNotEmpty()) VectorEmbeddingUtils.floatArrayToByteArray(vec) else null
                                val newFact = FactKnowledgeEntity(
                                    predicate = predicate,
                                    objectValue = capturedValue,
                                    confidence = 0.90f,
                                    lastUpdatedMillis = System.currentTimeMillis(),
                                    accessCount = 1,
                                    isPinned = false,
                                    embeddingBlob = blob
                                )
                                factDao.insertOrUpdateFact(newFact)
                                allFacts.add(newFact)
                            }
                        }
                    }
                }
            }

            val now = System.currentTimeMillis()
            for (fact in allFacts) {
                if (!fact.isPinned && fact.effectiveConfidence(now) < 0.15f) {
                    factDao.deleteFact(fact.id)
                    Log.d(TAG, "Pruned decayed ephemeral fact: ${fact.predicate} -> ${fact.objectValue}")
                }
            }

            Log.i(TAG, "Memory consolidation cycle complete.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Memory consolidation failed", e)
            Result.retry()
        }
    }
}
