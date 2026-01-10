package com.elink.aigallery.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class ClipHelper private constructor(context: Context) {

    private var imageInterpreter: Interpreter? = null
    private var textInterpreter: Interpreter? = null
    
    // Config for standard CLIP (ViT-B/32 or similar mobile variants)
    private val IMAGE_INPUT_SIZE = 224
    private val EMBEDDING_SIZE = 512
    private val CONTEXT_LENGTH = 77 // Standard CLIP max token length

    // Buffers
    private val imageInputBuffer: ByteBuffer
    private val imageOutputBuffer: Array<FloatArray>
    private val textInputBuffer: Array<IntArray> // [1, 77]
    private val textOutputBuffer: Array<FloatArray>

    init {
        // Initialize Interpreters safely
        try {
            imageInterpreter = Interpreter(loadModelFile(context, "clip_image_encoder.tflite"))
            Log.d(TAG, "CLIP Image Encoder loaded successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load CLIP Image Encoder: ${e.message}")
        }

        try {
            textInterpreter = Interpreter(loadModelFile(context, "clip_text_encoder.tflite"))
            Log.d(TAG, "CLIP Text Encoder loaded successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load CLIP Text Encoder: ${e.message}")
        }

        // 4 bytes * H * W * 3 (RGB)
        imageInputBuffer = ByteBuffer.allocateDirect(4 * IMAGE_INPUT_SIZE * IMAGE_INPUT_SIZE * 3)
            .order(ByteOrder.nativeOrder())
        
        imageOutputBuffer = Array(1) { FloatArray(EMBEDDING_SIZE) }
        
        textInputBuffer = Array(1) { IntArray(CONTEXT_LENGTH) }
        textOutputBuffer = Array(1) { FloatArray(EMBEDDING_SIZE) }
    }

    /**
     * Generates an embedding vector for the given image.
     */
    @Synchronized
    fun embedImage(bitmap: Bitmap): FloatArray? {
        if (imageInterpreter == null) return null

        imageInputBuffer.rewind()
        
        // Resize
        val resized = Bitmap.createScaledBitmap(bitmap, IMAGE_INPUT_SIZE, IMAGE_INPUT_SIZE, true)
        
        // Normalize (Standard CLIP mean/std)
        // Mean: [0.48145466, 0.4578275, 0.40821073]
        // Std:  [0.26862954, 0.26130258, 0.27577711]
        val mean = floatArrayOf(0.48145466f, 0.4578275f, 0.40821073f)
        val std = floatArrayOf(0.26862954f, 0.26130258f, 0.27577711f)

        for (y in 0 until IMAGE_INPUT_SIZE) {
            for (x in 0 until IMAGE_INPUT_SIZE) {
                val pixel = resized.getPixel(x, y)
                // RGB order
                val r = (Color.red(pixel) / 255.0f - mean[0]) / std[0]
                val g = (Color.green(pixel) / 255.0f - mean[1]) / std[1]
                val b = (Color.blue(pixel) / 255.0f - mean[2]) / std[2]

                imageInputBuffer.putFloat(r)
                imageInputBuffer.putFloat(g)
                imageInputBuffer.putFloat(b)
            }
        }
        
        if (resized != bitmap) {
            resized.recycle()
        }

        imageInterpreter?.run(imageInputBuffer, imageOutputBuffer)
        return normalizeVector(imageOutputBuffer[0].clone())
    }

    /**
     * Generates an embedding vector for the given text.
     */
    @Synchronized
    fun embedText(text: String): FloatArray? {
        if (textInterpreter == null) return null

        // 1. Tokenize
        val tokens = simpleTokenizer(text)
        
        // 2. Fill Input Buffer
        for (i in 0 until CONTEXT_LENGTH) {
            textInputBuffer[0][i] = if (i < tokens.size) tokens[i] else 0 // Padding with 0
        }

        // 3. Run Inference
        textInterpreter?.run(textInputBuffer, textOutputBuffer)
        
        return normalizeVector(textOutputBuffer[0].clone())
    }

    /**
     * A PLACEHOLDER Tokenizer.
     * Real CLIP requires a BPE tokenizer with a vocabulary file.
     * This simply maps characters/words to hash codes for demonstration
     * or assumes a very specific simple model.
     * 
     * TODO: Implement real BPE Tokenizer loading 'vocab.json'
     */
    private fun simpleTokenizer(text: String): IntArray {
        // Start token (49406) and End token (49407) are standard for CLIP
        val startToken = 49406
        val endToken = 49407
        
        val words = text.lowercase().split("\\s+".toRegex())
        val tokenList = mutableListOf<Int>()
        
        tokenList.add(startToken)
        // Very naive mapping for prototype
        for (word in words) {
            // Ideally: lookup word in map
            // Here: Just a stable hash to prevent crash, likely meaningless for the model
            val token = (word.hashCode() % 10000 + 10000) % 10000 
            tokenList.add(token)
        }
        tokenList.add(endToken)
        
        // Truncate if too long
        return if (tokenList.size > CONTEXT_LENGTH) {
             tokenList.take(CONTEXT_LENGTH).toIntArray()
        } else {
             tokenList.toIntArray()
        }
    }

    private fun normalizeVector(v: FloatArray): FloatArray {
        var sumSq = 0.0f
        for (valX in v) {
            sumSq += valX * valX
        }
        val norm = sqrt(sumSq)
        if (norm > 0) {
            for (i in v.indices) {
                v[i] /= norm
            }
        }
        return v
    }

    private fun loadModelFile(context: Context, fileName: String): ByteBuffer {
        context.assets.openFd(fileName).use { fileDescriptor ->
            FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
                val fileChannel = inputStream.channel
                val startOffset = fileDescriptor.startOffset
                val declaredLength = fileDescriptor.declaredLength
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            }
        }
    }

    companion object {
        private const val TAG = "ClipHelper"

        @Volatile
        private var INSTANCE: ClipHelper? = null

        fun getInstance(context: Context): ClipHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ClipHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
