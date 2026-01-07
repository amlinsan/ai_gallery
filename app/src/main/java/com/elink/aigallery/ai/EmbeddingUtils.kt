package com.elink.aigallery.ai

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

object EmbeddingUtils {
    fun toByteArray(vector: FloatArray): ByteArray {
        val buffer = ByteBuffer.allocate(vector.size * FLOAT_BYTES).order(ByteOrder.LITTLE_ENDIAN)
        vector.forEach { buffer.putFloat(it) }
        return buffer.array()
    }

    fun toFloatArray(bytes: ByteArray): FloatArray {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val floats = FloatArray(bytes.size / FLOAT_BYTES)
        for (i in floats.indices) {
            floats[i] = buffer.getFloat()
        }
        return floats
    }

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dot = 0f
        var normA = 0f
        var normB = 0f
        val size = minOf(a.size, b.size)
        for (i in 0 until size) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        return if (normA == 0f || normB == 0f) 0f else dot / (sqrt(normA) * sqrt(normB))
    }

    fun mergeAverage(old: FloatArray, oldCount: Int, fresh: FloatArray): FloatArray {
        val result = FloatArray(old.size)
        val count = maxOf(oldCount, 1)
        for (i in result.indices) {
            result[i] = (old[i] * count + fresh[i]) / (count + 1)
        }
        return result
    }

    private const val FLOAT_BYTES = 4
}
