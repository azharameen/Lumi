package com.example.data.firebase

import android.util.Log
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

/**
 * Manages Firebase Performance Monitoring to benchmark AI generation latency,
 * database sync times, sensor perception processing, and cold start performance.
 */
class LumiPerformanceManager {

    private val perf: FirebasePerformance? by lazy {
        try {
            FirebasePerformance.getInstance().apply {
                isPerformanceCollectionEnabled = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Performance is not available: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "LumiPerformance"

        // Standard Trace Names
        const val TRACE_AI_INFERENCE = "trace_ai_inference_time"
        const val TRACE_VISION_PROCESSING = "trace_vision_processing"
        const val TRACE_LOCAL_DB_QUERY = "trace_local_db_query"
        const val TRACE_REMOTE_CONFIG_FETCH = "trace_remote_config_fetch"
        const val TRACE_DAILY_BRIEFING_GEN = "trace_daily_briefing_gen"
    }

    /**
     * Starts a custom Firebase Performance trace.
     */
    fun startTrace(traceName: String): Trace? {
        return try {
            val trace = perf?.newTrace(traceName)
            trace?.start()
            trace
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start trace: $traceName", e)
            null
        }
    }

    /**
     * Executes a synchronous code block wrapped within a Performance Trace.
     */
    fun <T> trace(traceName: String, block: () -> T): T {
        val trace = startTrace(traceName)
        return try {
            block()
        } finally {
            try {
                trace?.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop trace: $traceName", e)
            }
        }
    }

    /**
     * Executes a suspending asynchronous code block wrapped within a Performance Trace.
     */
    suspend fun <T> traceAsync(traceName: String, block: suspend () -> T): T {
        val trace = startTrace(traceName)
        return try {
            block()
        } finally {
            try {
                trace?.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop trace: $traceName", e)
            }
        }
    }

    /**
     * Executes a suspending asynchronous code block wrapped within a Performance Trace with custom attributes and metrics.
     */
    suspend fun <T> traceAsyncWithAttributes(
        traceName: String,
        attributes: Map<String, String> = emptyMap(),
        metrics: Map<String, Long> = emptyMap(),
        block: suspend (Trace?) -> T
    ): T {
        val trace = startTrace(traceName)
        try {
            attributes.forEach { (k, v) ->
                try { trace?.putAttribute(k, v) } catch (_: Exception) {}
            }
            metrics.forEach { (k, v) ->
                try { trace?.putMetric(k, v) } catch (_: Exception) {}
            }
            return block(trace)
        } finally {
            try {
                trace?.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop trace: $traceName", e)
            }
        }
    }
}
