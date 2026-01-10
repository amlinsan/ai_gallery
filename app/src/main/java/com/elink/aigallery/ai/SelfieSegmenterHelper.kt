package com.elink.aigallery.ai

import android.content.Context
import android.graphics.Bitmap
import com.elink.aigallery.utils.MyLog
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter
import org.tensorflow.lite.task.vision.segmenter.OutputType
import java.util.Locale

class SelfieSegmenterHelper private constructor(context: Context) {
    private val segmenter: ImageSegmenter? = try {
        createSegmenter(context)
    } catch (e: Exception) {
        MyLog.e(TAG, "Segmentation model is not available", e)
        null
    }

    @Synchronized
    fun segment(bitmap: Bitmap): SegmentationMask? {
        val localSegmenter = segmenter ?: return null
        return try {
            val input = TensorImage.fromBitmap(bitmap)
            val results = localSegmenter.segment(input)
            val segmentation = results.firstOrNull() ?: return null
            val masks = segmentation.masks
            if (masks.isEmpty()) return null

            val mask = selectPersonMask(segmentation.masks, segmentation.coloredLabels)
            val buffer = mask.tensorBuffer
            val floatMask = when (buffer.dataType) {
                DataType.UINT8 -> buffer.intArray.map { it / 255f }.toFloatArray()
                else -> buffer.floatArray
            }
            SegmentationMask(
                values = floatMask,
                width = mask.width,
                height = mask.height
            )
        } catch (e: Exception) {
            MyLog.e(TAG, "Selfie segmentation failed", e)
            null
        }
    }

    fun close() {
        segmenter?.close()
    }

    private fun createSegmenter(context: Context): ImageSegmenter {
        val baseOptions = BaseOptions.builder()
            .setNumThreads(NUM_THREADS)
            .build()
        val options = ImageSegmenter.ImageSegmenterOptions.builder()
            .setBaseOptions(baseOptions)
            .setOutputType(OutputType.CONFIDENCE_MASK)
            .build()
        return ImageSegmenter.createFromFileAndOptions(
            context.applicationContext,
            MODEL_FILE,
            options
        )
    }

    private fun selectPersonMask(
        masks: List<TensorImage>,
        labels: List<org.tensorflow.lite.task.vision.segmenter.ColoredLabel>
    ): TensorImage {
        if (masks.size == 1 || labels.isEmpty()) {
            return masks[0]
        }
        val index = labels.indexOfFirst {
            val name = "${it.displayName}".lowercase(Locale.US)
            name.contains(PERSON_LABEL) || name.contains(FOREGROUND_LABEL)
        }.let { if (it >= 0 && it < masks.size) it else masks.lastIndex }
        return masks[index]
    }

    data class SegmentationMask(
        val values: FloatArray,
        val width: Int,
        val height: Int
    )

    companion object {
        private const val TAG = "SelfieSegmenterHelper"
        private const val MODEL_FILE = "selfie_segmenter.tflite"
        private const val NUM_THREADS = 2
        private const val PERSON_LABEL = "person"
        private const val FOREGROUND_LABEL = "foreground"

        @Volatile
        private var INSTANCE: SelfieSegmenterHelper? = null

        fun getInstance(context: Context): SelfieSegmenterHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SelfieSegmenterHelper(context).also { INSTANCE = it }
            }
        }
    }
}
