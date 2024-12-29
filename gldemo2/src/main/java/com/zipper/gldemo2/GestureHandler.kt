package com.zipper.gldemo2

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.OverScroller
import android.widget.Scroller
import kotlin.math.max
import kotlin.math.min

/**
 *
 * @author  zhangzhipeng
 * @date    2024/4/8
 */
class GestureHandler(
    private val context: Context,
    private val gestureCallback: IGestureCallback
) : ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {
    private enum class State {
        None,
        Zone,
        Drag,
    }

    private val scaleGestureDetector = ScaleGestureDetector(context, this)

    private val gestureDetector = GestureDetector(context, this)

    private val scroller: OverScroller = OverScroller(context).apply {
        setFriction(0.05f)
    }

    private val flingRunnable = FlingRunnable()

    private var state = State.None


    fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.action == MotionEvent.ACTION_DOWN) {
            state = State.Drag
        } else if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            Log.e("BAAA", "ACTION_POINTER_DOWN")
            state = State.Zone
        } else if (event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
            state = State.None
        }

        if (state == State.Drag) {
            gestureDetector.onTouchEvent(event)
        } else if (state == State.Zone) {
            scaleGestureDetector.onTouchEvent(event)
        }

//        val eventHandle = scaleGestureDetector.onTouchEvent(event)
//        Log.e("BAAA", "onTouchEvent = $eventHandle ")
//        if (!scaleGestureDetector.isInProgress) {
//            gestureDetector.onTouchEvent(event)
//        }
        return true
    }

    /// ========================================================================

    private var isScale = false

    /**
     * 最小缩放倍数
     */
    private var minScale = 1f

    /**
     * 最大缩放倍数
     */
    private val maxScale = 8f
    private var currentScale = 1f

    /**
     * @return true 表示缩放事件被处理了，检测器会重新累计缩放因子， false继续累计缩放因子
     */
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        currentScale *= detector.scaleFactor
        currentScale = max(minScale, currentScale)
        currentScale = min(currentScale, maxScale)

        gestureCallback.onScale(currentScale, detector.focusX, detector.focusY)
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        isScale = true
        Log.e("EventState", "onScaleBegin scale = $currentScale")
        gestureCallback.onScaleStart(currentScale, detector.focusX, detector.focusY)
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        Log.e("EventState", "Gesture onScaleEnd")
        isScale = false
    }

    /// ========================================================================

    private var isDown = false

    override fun onDown(e: MotionEvent): Boolean {
        this.isDown = true
        Log.e("EventState", "onDown x = ${e.x} y = ${e.y}")
        gestureCallback.cancelTaskOnMain(flingRunnable)
        return true
    }

    override fun onShowPress(e: MotionEvent) {
//        Log.e("EventState", "onShowPress 按下")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.e("EventState", "onSingleTapUp 在touch down 后没有滑动（onScroll）又没有长按（onLongPress）的情况下触发")
        if (isDown) {
            gestureCallback.onSingleTapUp(e.x, e.y)
            isDown = false
        }
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        Log.e("EventState", "onScroll distanceX = $distanceX distanceY = $distanceY")
        offsetX -= distanceX
        offsetY -= distanceY
        gestureCallback.cancelTaskOnMain(flingRunnable)
        gestureCallback.onScroll(-distanceX, distanceY)
        return true
    }

    override fun onLongPress(e: MotionEvent) = Unit

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val pointCount1 = e1?.pointerCount ?: 0
        val pointCount2 = e2.pointerCount
        if (pointCount1 > 1 || pointCount2 > 1) {
            return false
        }
        Log.e("EventState", "Gesture onFling 滑动")
        scroller.fling(
            offsetX.toInt(), offsetY.toInt(),
            velocityX.toInt(), velocityY.toInt(),
            Integer.MIN_VALUE, Integer.MAX_VALUE,
            Integer.MIN_VALUE, Integer.MAX_VALUE,
        )
        gestureCallback.runTaskOnMain(flingRunnable)
        return false
    }

    private var offsetX = 0f
    private var offsetY = 0f

    inner class FlingRunnable : Runnable {

        override fun run() {
            if (scroller.isFinished) {
                return
            }

            if (scroller.computeScrollOffset()) {
                val dx = offsetX - scroller.currX * 1.0f
                val dy = offsetY - scroller.currY * 1.0f
                offsetX = scroller.currX * 1.0f
                offsetY = scroller.currY * 1.0f
                gestureCallback.onScroll(-dx, dy)
                gestureCallback.runTaskOnMain(this)
            }
        }
    }
}