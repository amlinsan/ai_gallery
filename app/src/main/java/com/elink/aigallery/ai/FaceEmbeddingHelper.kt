package com.elink.aigallery.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class FaceEmbeddingHelper private constructor(context: Context) {
    private val interpreter: Interpreter
    private val inputBuffer: ByteBuffer
    private val outputBuffer: Array<FloatArray>

    init {
        interpreter = Interpreter(loadModelFile(context))
        inputBuffer = ByteBuffer
            .allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * INPUT_CHANNELS)
            .order(ByteOrder.nativeOrder())
        outputBuffer = Array(1) { FloatArray(EMBEDDING_DIM) }
    }

    @Synchronized
    fun embed(bitmap: Bitmap): FloatArray {
        inputBuffer.rewind()
        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val pixel = resized.getPixel(x, y)
                inputBuffer.putFloat((Color.red(pixel) - IMAGE_MEAN) / IMAGE_STD)
                inputBuffer.putFloat((Color.green(pixel) - IMAGE_MEAN) / IMAGE_STD)
                inputBuffer.putFloat((Color.blue(pixel) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        if (resized != bitmap) {
            resized.recycle()
        }
        interpreter.run(inputBuffer, outputBuffer)
        return outputBuffer[0].clone()
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        context.assets.openFd(MODEL_FILE).use { fileDescriptor ->
            FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
                val fileChannel = inputStream.channel
                val startOffset = fileDescriptor.startOffset
                val declaredLength = fileDescriptor.declaredLength
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            }
        }
    }

    companion object {
        private const val MODEL_FILE = "face_embedding.tflite"
        private const val INPUT_SIZE = 160
        private const val INPUT_CHANNELS = 3
        private const val EMBEDDING_DIM = 128
        private const val IMAGE_MEAN = 127.5f
        private const val IMAGE_STD = 128f

        @Volatile
        private var INSTANCE: FaceEmbeddingHelper? = null

        fun getInstance(context: Context): FaceEmbeddingHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FaceEmbeddingHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
