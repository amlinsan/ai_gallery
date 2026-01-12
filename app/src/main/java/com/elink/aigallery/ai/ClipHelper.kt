package com.elink.aigallery.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.elink.aigallery.utils.MyLog
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class ClipHelper private constructor(context: Context) {

    private var imageInterpreter: Interpreter? = null
    private var textInterpreter: Interpreter? = null
    private var tokenizer: ClipTokenizer? = null
    
    // Config
    private var imageInputSize = 224 // Default, will be overwritten by model
    private val EMBEDDING_SIZE = 512
    private var textContextLength = 77
    private var textInputIsInt64 = false

    // Buffers
    private var imageInputBuffer: ByteBuffer
    private val imageOutputBuffer: Array<FloatArray>
    private var textInputIntBuffer: Array<IntArray>? = null
    private var textInputLongBuffer: Array<LongArray>? = null
    private val textOutputBuffer: Array<FloatArray>

    init {
        // Initialize Interpreters
        var loadedImageInterpreter: Interpreter? = null
        try {
            loadedImageInterpreter = Interpreter(loadModelFile(context, "clip_image_encoder.tflite"))
            MyLog.i(TAG, "CLIP image encoder loaded.")
            
            // Read input shape from model (index 0)
            val inputTensor = loadedImageInterpreter.getInputTensor(0)
            val inputShape = inputTensor.shape() // [1, 224, 224, 3] or similar
            // Usually shape is [batch, height, width, channels]
            if (inputShape.size == 4) {
                imageInputSize = inputShape[1] // Assuming Square: Height
                MyLog.i(TAG, "Model input size detected: $imageInputSize")
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "Failed to load CLIP image encoder: ${e.message}", e)
        }
        imageInterpreter = loadedImageInterpreter

        try {
            textInterpreter = Interpreter(loadModelFile(context, "clip_text_encoder.tflite"))
            MyLog.i(TAG, "CLIP text encoder loaded.")
            val inputTensor = textInterpreter?.getInputTensor(0)
            if (inputTensor != null) {
                val inputShape = inputTensor.shape()
                if (inputShape.size >= 2) {
                    textContextLength = inputShape[1]
                }
                textInputIsInt64 = inputTensor.dataType() == DataType.INT64
                MyLog.i(
                    TAG,
                    "CLIP text input detected: len=$textContextLength type=${inputTensor.dataType()}"
                )
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "Failed to load CLIP text encoder: ${e.message}", e)
        }

        try {
            tokenizer = ClipTokenizer(context, contextLength = textContextLength)
            MyLog.i(TAG, "CLIP tokenizer loaded.")
        } catch (e: Exception) {
            MyLog.e(TAG, "Failed to load CLIP tokenizer: ${e.message}", e)
        }

        // Allocate Buffer based on detected size
        // 4 bytes (float) * H * W * 3 (RGB)
        imageInputBuffer = ByteBuffer.allocateDirect(4 * imageInputSize * imageInputSize * 3)
            .order(ByteOrder.nativeOrder())
        
        imageOutputBuffer = Array(1) { FloatArray(EMBEDDING_SIZE) }
        
        if (textInputIsInt64) {
            textInputLongBuffer = Array(1) { LongArray(textContextLength) }
        } else {
            textInputIntBuffer = Array(1) { IntArray(textContextLength) }
        }
        textOutputBuffer = Array(1) { FloatArray(EMBEDDING_SIZE) }
    }

    /**
     * Generates an embedding vector for the given image.
     */
    @Synchronized
    fun embedImage(bitmap: Bitmap): FloatArray? {
        if (imageInterpreter == null) return null

        imageInputBuffer.rewind()
        
        // Resize to model's expected size
        val resized = Bitmap.createScaledBitmap(bitmap, imageInputSize, imageInputSize, true)
        
        // Normalize (Standard CLIP mean/std)
        // Mean: [0.48145466, 0.4578275, 0.40821073]
        // Std:  [0.26862954, 0.26130258, 0.27577711]
        val mean = floatArrayOf(0.48145466f, 0.4578275f, 0.40821073f)
        val std = floatArrayOf(0.26862954f, 0.26130258f, 0.27577711f)

        for (y in 0 until imageInputSize) {
            for (x in 0 until imageInputSize) {
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
        val tokens = tokenizer?.encode(text) ?: return null
        
        // 2. Fill Input Buffer
        if (textInputIsInt64) {
            val longInput = textInputLongBuffer ?: return null
            for (i in 0 until textContextLength) {
                longInput[0][i] = tokens[i].toLong()
            }

            // 3. Run Inference
            textInterpreter?.run(longInput, textOutputBuffer)
        } else {
            val intInput = textInputIntBuffer ?: return null
            for (i in 0 until textContextLength) {
                intInput[0][i] = tokens[i]
            }

            // 3. Run Inference
            textInterpreter?.run(intInput, textOutputBuffer)
        }
        
        return normalizeVector(textOutputBuffer[0].clone())
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
