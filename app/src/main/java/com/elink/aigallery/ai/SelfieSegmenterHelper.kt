package com.elink.aigallery.ai

import android.content.Context
import android.graphics.Bitmap
import com.elink.aigallery.utils.MyLog
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.ByteBufferExtractor
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter
import java.nio.ByteOrder
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
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = localSegmenter.segment(mpImage)
            val personIndex = findPersonLabelIndex(localSegmenter.labels)
            val confidenceMasks = result.confidenceMasks().orElse(null)
            val fallbackIndex = personIndex ?: PERSON_CATEGORY_ID
            val confidenceMask = when {
                confidenceMasks != null && confidenceMasks.size > fallbackIndex ->
                    confidenceMasks[fallbackIndex]
                confidenceMasks != null && confidenceMasks.isNotEmpty() ->
                    confidenceMasks.first()
                else -> null
            }
            if (confidenceMask != null) {
                buildMaskFromConfidence(confidenceMask, bitmap.width, bitmap.height)
                    ?.let { return it }
            }

            val categoryMask = result.categoryMask().orElse(null)
            if (categoryMask == null) {
                MyLog.w(TAG, "Segmentation returned empty masks")
                return null
            }
            buildMaskFromCategory(categoryMask, bitmap.width, bitmap.height, personIndex)
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
            .setModelAssetPath(MODEL_FILE)
            .build()
        val options = ImageSegmenter.ImageSegmenterOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setOutputConfidenceMasks(true)
            .setOutputCategoryMask(true)
            .build()
        return ImageSegmenter.createFromOptions(context.applicationContext, options)
    }

    private fun buildMaskFromCategory(
        maskImage: MPImage,
        fallbackWidth: Int,
        fallbackHeight: Int,
        personIndex: Int?
    ): SegmentationMask? {
        val width = if (maskImage.width > 0) maskImage.width else fallbackWidth
        val height = if (maskImage.height > 0) maskImage.height else fallbackHeight
        val total = width * height
        val buffer = ByteBufferExtractor.extract(maskImage, MPImage.IMAGE_FORMAT_ALPHA)
        if (buffer == null || !buffer.hasRemaining()) {
            MyLog.w(TAG, "Segmentation mask buffer unavailable")
            return null
        }
        buffer.rewind()
        val values = FloatArray(total)
        if (buffer.remaining() < total) {
            MyLog.w(TAG, "Segmentation mask buffer too small size=${buffer.remaining()} need=$total")
            return null
        }
        for (i in 0 until total) {
            val label = buffer.get().toInt() and 0xFF
            values[i] = if (personIndex != null) {
                if (label == personIndex) 1f else 0f
            } else {
                if (label == 0) 0f else 1f
            }
        }
        return SegmentationMask(values, width, height)
    }

    private fun findPersonLabelIndex(labels: List<String>): Int? {
        if (labels.isEmpty()) return null
        return labels.indexOfFirst { label ->
            val normalized = label.lowercase(Locale.US)
            normalized.contains(PERSON_LABEL) || normalized.contains(FOREGROUND_LABEL)
        }.takeIf { it >= 0 }
    }

    private fun buildMaskFromConfidence(
        maskImage: MPImage,
        fallbackWidth: Int,
        fallbackHeight: Int
    ): SegmentationMask? {
        val width = if (maskImage.width > 0) maskImage.width else fallbackWidth
        val height = if (maskImage.height > 0) maskImage.height else fallbackHeight
        val total = width * height
        val buffer = ByteBufferExtractor.extract(maskImage, MPImage.IMAGE_FORMAT_VEC32F1)
        if (buffer == null || !buffer.hasRemaining()) {
            MyLog.w(TAG, "Segmentation confidence buffer unavailable")
            return null
        }
        buffer.order(ByteOrder.nativeOrder())
        val floatBuffer = buffer.asFloatBuffer()
        if (floatBuffer.remaining() < total) {
            MyLog.w(TAG, "Segmentation confidence buffer too small size=${floatBuffer.remaining()} need=$total")
            return null
        }
        val values = FloatArray(total)
        for (i in 0 until total) {
            values[i] = floatBuffer.get().coerceIn(0f, 1f)
        }
        return SegmentationMask(values, width, height)
    }

    data class SegmentationMask(
        val values: FloatArray,
        val width: Int,
        val height: Int
    )

    companion object {
        private const val TAG = "SelfieSegmenterHelper"
        private const val MODEL_FILE = "selfie_segmenter.tflite"
        private const val PERSON_CATEGORY_ID = 1
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
