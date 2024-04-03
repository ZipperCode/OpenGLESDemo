package com.zipper.opengl

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import kotlinx.coroutines.Runnable
import kotlin.math.abs

/**
 *
 * @author  zhangzhipeng
 * @date    2024/4/3
 */
class GestureHandler(
    private val surfaceView: ImageGLSurfaceView,
    private val render: MyGLSurfaceRender
) : GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {

    private enum class State {
        None,
        Zone,
        Drag
    }

    private var state = State.None

    private var preScale = 1f

    private var minScale = 1f

    private var maxScale = 10f

    private val scaleGestureDetector = ScaleGestureDetector(surfaceView.context, this)

    private val gestureDetector = GestureDetector(surfaceView.context, this)

    private val scaledTouchSlop = ViewConfiguration.get(surfaceView.context).scaledTouchSlop

    private val scroller = DragScroller(surfaceView.context)

    private val flingRunnable = FlingRunnable()

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            state = State.Zone
        } else if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            state = State.Zone
        }

        if (event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
            state = State.None
        }

        if (state === State.Zone) {
//            scaleGestureDetector.onTouchEvent(event)
//            return true
        } else if (state === State.Drag) {
            gestureDetector.onTouchEvent(event)
        }

        return true
    }

    /**
     * 按下的位置
     */
    private var lastDownX = 0f
    private var lastDownY = 0f

    override fun onDown(e: MotionEvent): Boolean {
        Log.d("BAAA", "GestureListenerImpl onDown")
        lastDownX = e.x
        lastDownY = e.y
        pointEndX = e.x
        pointEndY = e.y
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        Log.d("BAAA", "onShowPress 按下")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.d("BAAA", "onSingleTapUp 在touch down 后没有滑动（onScroll）又没有长按（onLongPress）的情况下触发")
        return false
    }

    /**
     * 手指最后停留的位置
     */
    private var pointEndX = 0f
    private var pointEndY = 0f

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        pointEndX -= distanceX
        pointEndY -= distanceY
        render.onScroll(distanceX / surfaceView.width, distanceY / surfaceView.height)
        surfaceView.requestRender()
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        Log.d("BAAA", "onLongPress 长按")
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        Log.d("BAAA", "onFling 滑动")
        val pointCount1 = e1?.pointerCount ?: 0
        val pointCount2 = e2.pointerCount
        if (pointCount1 > 1 || pointCount2 > 1) {
            return false
        }
        flingRunnable.onFling(pointEndX.toInt(), pointEndY.toInt(), -velocityX.toInt(), -velocityY.toInt())
        postOnAnimation(flingRunnable)
        return false
    }

    fun handleFlingUpdate(distanceX: Int, distanceY: Int) {

    }

    fun postOnAnimation(runnable: Runnable) {
        surfaceView.postOnAnimation(runnable)
    }

    private var initialSpan = 0f
    private var currentSpan = 0f

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        currentSpan = detector.currentSpan
        Log.d("BAAA", "onScale currentSpan = $currentSpan initialSpan = $initialSpan")
        if (abs(currentSpan - initialSpan) > 10) {
            var currentScale = detector.scaleFactor * preScale
            Log.d("BAAA", "onScale currentScale = $currentScale")
            if (currentScale < minScale) {
                currentScale = minScale
            } else if (currentScale > maxScale) {
                currentScale = maxScale
            }

            preScale = currentScale
        }
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        initialSpan = detector.currentSpan
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) = Unit


    inner class FlingRunnable : Runnable {

        private var currentX = 0
        private var currentY = 0

        fun onFling(startX: Int, startY: Int, velocityX: Int, velocityY: Int) {
            currentX = startX
            currentY = startY
            scroller.fling(
                startX, startY,
                velocityX, velocityY,
                0, 100,
                0, 100
            )
        }

        override fun run() {
            if (scroller.isFinished) {
                return
            }
            if (scroller.computeScrollOffset()) {
                val x = scroller.currX
                val y = scroller.currY
                handleFlingUpdate(currentX - x, currentY - y)
                currentX = x
                currentY = y
                postOnAnimation(this)
            }
        }
    }
}