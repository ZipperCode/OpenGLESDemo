package com.zipper.gldemo2

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.max
import kotlin.math.min

/**
 *
 * @author  zhangzhipeng
 * @date    2024/4/8
 */
class GestureHandler(
    private val gestureCallback: IGestureCallback
) : ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

    private val scaleGestureDetector = ScaleGestureDetector(gestureCallback.requireContext(), this)

    /**
     * 当前缩放倍数
     */
    private var currentScale = 1f

    /**
     * 最小缩放倍数
     */
    private var minScale = 0.7f

    /**
     * 最大缩放倍数
     */
    private var maxScale = 3f

    /**
     * 缩放中心点X
     */
    private var scaleFocusX = 0f

    /**
     * 缩放中心点Y
     */
    private var scaleFocusY = 0f

    /**
     * view矩阵值
     */
    private val graphicsMatrixArray = FloatArray(9)

    /**
     * view矩阵
     */
    private val graphicsMatrix = Matrix()

    private val viewRect = RectF()

    private val glMapMatrix = Matrix()

    private val glRect = RectF(-1f, -1f, 1f, 1f)

    fun onViewSizeChange(width: Int, height: Int) {
        viewRect.set(0f, 0f, width.toFloat(), height.toFloat())
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    /// ========================================================================

    /**
     * @return true 表示缩放事件被处理了，检测器会重新累计缩放因子， false继续累计缩放因子
     */
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        currentScale *= detector.scaleFactor
        currentScale = max(minScale, currentScale)
        currentScale = min(currentScale, maxScale)
        scaleFocusX = detector.focusX
        scaleFocusY = detector.focusY
        val scale = detector.scaleFactor - currentScale
        graphicsMatrix.postScale(scale, scale, scaleFocusX, scaleFocusY)
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        Log.e("EventState", "Gesture onScaleEnd currentScale")
    }

    /// ========================================================================

    private var offsetX = 0f
    private var offsetY = 0f

    override fun onDown(e: MotionEvent): Boolean {
        Log.e("EventState", "GestureListenerImpl onDown x = ${e.x} y = ${e.y}")
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        Log.e("EventState", "onShowPress 按下")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.e("EventState", "onSingleTapUp 在touch down 后没有滑动（onScroll）又没有长按（onLongPress）的情况下触发")


        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        TODO("Not yet implemented")
    }

    override fun onLongPress(e: MotionEvent) = Unit

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    fun updateTranslate(distanceX: Float, distanceY: Float) {

        offsetX -= distanceX
        offsetY -= distanceY

    }

}