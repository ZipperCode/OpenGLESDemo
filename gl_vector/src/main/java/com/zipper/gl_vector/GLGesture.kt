package com.zipper.gl_vector

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Scroller
import kotlin.math.max
import kotlin.math.min

class GLGesture(
    private val context: Context
) : ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener, View.OnTouchListener {

    private enum class State {
        None,
        Zone,
        Drag,
    }

    private var state = State.None

    private val scaleGestureDetector = ScaleGestureDetector(context, this)

    private val gestureDetector = GestureDetector(context, this, Handler(Looper.getMainLooper()))

    private val scroller: Scroller = Scroller(context)

    private val minScale = 1f

    private val maxScale = 5f

    private var currentScale = 1f

    /**
     * 是否处于缩放动画过程中
     */
    private var inScaleAnim = false

    private var isDown = false

    private var isScale = false

    var callback: Callback? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event ?: return false
        if (event.action == MotionEvent.ACTION_DOWN) {
            state = State.Drag
        } else if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            state = State.Zone
        } else if (event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
            state = State.None
        }

        if (inScaleAnim) {
            // 播放放大缩小动画过程中，禁止手势操作
            // 缩放过程同时有修改offset的值，如果出现滑动，则会出现画面抖动
            return true
        }

        if (state == State.Drag) {
            gestureDetector.onTouchEvent(event)
        } else if (state == State.Zone) {
            scaleGestureDetector.onTouchEvent(event)
        }

        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        currentScale *= detector.scaleFactor
        currentScale = max(minScale, currentScale)
        currentScale = min(currentScale, maxScale)
        callback?.onScale(currentScale, detector.focusX, detector.focusY)
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        isScale = true
        callback?.onScale(currentScale, detector.focusX, detector.focusY)
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        isScale = false
    }

    override fun onDown(e: MotionEvent): Boolean {
        isDown = true
        return true
    }

    override fun onShowPress(e: MotionEvent) = Unit

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        if (isDown) {
            callback?.onSingleTapUp(e.x, e.y)
            isDown = false
        }

        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        callback?.onScroll(distanceX, distanceY)
        return true
    }

    override fun onLongPress(e: MotionEvent) = Unit

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    interface Callback {

        fun onScale(scale: Float, focusX: Float, focusY: Float)

        fun onScroll(dx: Float, dy: Float)

        fun onSingleTapUp(x: Float, y: Float)

    }
}