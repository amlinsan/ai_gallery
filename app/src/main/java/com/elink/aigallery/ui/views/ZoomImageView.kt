package com.elink.aigallery.ui.views

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class ZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), ScaleGestureDetector.OnScaleGestureListener,
    GestureDetector.OnGestureListener {

    private var scaleDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)
    private var gestureDetector: GestureDetector = GestureDetector(context, this)
    private var matrixCurrent = Matrix()
    private var mode = NONE
    private var last = PointF()
    private var start = PointF()
    private var minScale = 1f
    private var maxScale = 3f
    private var mSaveScale = 1f
    private var matrixValues: FloatArray = FloatArray(9)

    companion object {
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2
    }

    init {
        imageMatrix = matrixCurrent
        scaleType = ScaleType.MATRIX
        setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            val curr = PointF(event.x, event.y)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(curr)
                    start.set(last)
                    mode = DRAG
                }
                MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                    val deltaX = curr.x - last.x
                    val deltaY = curr.y - last.y
                    fixTranslation(deltaX, deltaY)
                    last.set(curr.x, curr.y)
                }
                MotionEvent.ACTION_UP -> {
                    mode = NONE
                    val xDiff = Math.abs(curr.x - start.x).toInt()
                    val yDiff = Math.abs(curr.y - start.y).toInt()
                    if (xDiff < 3 && yDiff < 3) performClick()
                }
                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }
            setImageMatrix(matrixCurrent)
            invalidate()
            true
        }
    }

    private fun fixTranslation(deltaX: Float, deltaY: Float) {
        val fixTransX = getFixDragTrans(deltaX, viewWidth.toFloat(), origWidth * mSaveScale)
        val fixTransY = getFixDragTrans(deltaY, viewHeight.toFloat(), origHeight * mSaveScale)
        matrixCurrent.postTranslate(fixTransX, fixTransY)
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) {
            0f
        } else {
            delta
        }
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        var mScaleFactor = detector.scaleFactor
        val prevScale = mSaveScale
        mSaveScale *= mScaleFactor
        if (mSaveScale > maxScale) {
            mSaveScale = maxScale
            mScaleFactor = maxScale / prevScale
        } else if (mSaveScale < minScale) {
            mSaveScale = minScale
            mScaleFactor = minScale / prevScale
        }

        if (origWidth * mSaveScale <= viewWidth || origHeight * mSaveScale <= viewHeight) {
            matrixCurrent.postScale(mScaleFactor, mScaleFactor, viewWidth / 2f, viewHeight / 2f)
        } else {
            matrixCurrent.postScale(mScaleFactor, mScaleFactor, detector.focusX, detector.focusY)
        }
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true
    override fun onScaleEnd(detector: ScaleGestureDetector) {}

    private var origWidth = 0f
    private var origHeight = 0f
    private var viewWidth = 0
    private var viewHeight = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (mSaveScale == 1f) {
            fitToScreen()
        }
    }

    private fun fitToScreen() {
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) return
        val bmWidth = drawable.intrinsicWidth.toFloat()
        val bmHeight = drawable.intrinsicHeight.toFloat()
        val scaleX = viewWidth.toFloat() / bmWidth
        val scaleY = viewHeight.toFloat() / bmHeight
        val scale = Math.min(scaleX, scaleY)
        matrixCurrent.setScale(scale, scale)
        
        // Center the image
        var redundantYSpace = viewHeight.toFloat() - scale * bmHeight
        var redundantXSpace = viewWidth.toFloat() - scale * bmWidth
        redundantYSpace /= 2f
        redundantXSpace /= 2f
        matrixCurrent.postTranslate(redundantXSpace, redundantYSpace)
        
        origWidth = viewWidth - 2 * redundantXSpace
        origHeight = viewHeight - 2 * redundantYSpace
        setImageMatrix(matrixCurrent)
    }

    fun resetZoom() {
        mSaveScale = 1f
        matrixCurrent.reset()
        imageMatrix = matrixCurrent
        mode = NONE
        if (viewWidth == 0 || viewHeight == 0) return
        fitToScreen()
        invalidate()
    }

    // GestureDetector needed for Double Tap
    override fun onDown(e: MotionEvent): Boolean = false
    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = false
    override fun onLongPress(e: MotionEvent) {}
    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = false
}
