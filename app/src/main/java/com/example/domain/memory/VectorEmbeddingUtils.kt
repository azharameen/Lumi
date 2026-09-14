package com.example.domain.memory

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

/**
 * High-performance FloatArray <-> ByteArray serialization and in-memory SIMD-like cosine similarity.
 * Eliminates NDK/C++ dependencies while providing sub-millisecond retrieval on thousands of vectors.
 */
object VectorEmbeddingUtils {

    fun floatArrayToByteArray(floats: FloatArray): ByteArray {
        val buffer = ByteBuffer.allocate(floats.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        for (f in floats) {
            buffer.putFloat(f)
        }
        return buffer.array()
    }

    fun byteArrayToFloatArray(bytes: ByteArray?): FloatArray {
        if (bytes == null || bytes.isEmpty()) return FloatArray(0)
        val count = bytes.size / 4
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val floats = FloatArray(count)
        for (i in 0 until count) {
            floats[i] = buffer.float
        }
        return floats
    }

    fun cosineSimilarity(vecA: FloatArray, vecB: FloatArray): Float {
        val minDim = minOf(vecA.size, vecB.size)
        if (minDim == 0) return 0f

        var dot = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in 0 until minDim) {
            val a = vecA[i].toDouble()
            val b = vecB[i].toDouble()
            dot += (a * b)
            normA += (a * a)
            normB += (b * b)
        }

        if (normA <= 0.0 || normB <= 0.0) return 0f
        val denom = sqrt(normA) * sqrt(normB)
        if (denom <= 1e-7) return 0f
        val res = ((dot / denom).coerceIn(-1.0, 1.0)).toFloat()
        return if (res.isNaN()) 0f else res
    }
}
