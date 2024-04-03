package com.zipper.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import com.zipper.opengl.utils.AssetsUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 *
 * @author zhangzhipeng
 * @date 2024/4/2
 */
class ScaleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GestureDetector.OnGestureListener, OnScaleGestureListener {

    companion object {
        const val MAX_SCALE = 3.0f
        const val MIN_SCALE = 0.5f
    }

    private val mGestureDetector: GestureDetector = GestureDetector(context, this)

    private val mScaleGestureDetector = ScaleGestureDetector(context, this)

    private val matrixArr = FloatArray(9)

    private val scaleMatrix = Matrix()

    private val matrix = Matrix()

    private val scroller = DragScroller(context)

    private val flingRunnable = FlingRunnable()

    private val bitmap: Bitmap = AssetsUtil.getAssetsImage(
        context,
        "902684/maskCombine.png"
    )


    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(event)
        if (!mScaleGestureDetector.isInProgress) {
            mGestureDetector.onTouchEvent(event)
        }
        return true
    }

    override fun draw(canvas: Canvas) {
        canvas.setMatrix(matrix)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        super.draw(canvas)
    }

    private var isScroll = false

    private var downX = 0
    private var downY = 0

    private var scrollX = 0
    private var scrollY = 0

    override fun onDown(e: MotionEvent): Boolean {
        Log.d("BAAA", "onDown 按下")
        downX = e.x.toInt()
        downY = e.y.toInt()
        scrollX = downX
        scrollY = downY
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        Log.d("BAAA", "onShowPress 按下")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.d("BAAA", "onSingleTapUp 在touch down 后没有滑动（onScroll）又没有长按（onLongPress）的情况下触发")
        return false
    }



    override fun onLongPress(e: MotionEvent) {
        Log.d("BAAA", "onLongPress 长按")
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        isScroll = true

        scrollX -= distanceX.toInt()
        scrollY -= distanceY.toInt()
        Log.d("BAAA", "onScroll 滑动 distanceX = = $distanceX, distanceY == $distanceY scrollX = $scrollX scrollY = $scrollY")

        matrix.postTranslate(-distanceX, -distanceY)
        invalidate()
        return false
    }

    private fun canScroll(distanceX: Float, distanceY: Float): Boolean {
        matrix.getValues(matrixArr)
        var transX = matrixArr.get(Matrix.MTRANS_X)
        var transY = matrixArr.get(Matrix.MTRANS_Y)
        transX -= distanceX
        transY -= distanceY
        Log.d("BAAA", "canScroll w = $transX h = $transY")
        if (abs(transX * 2) >= width) {
            return false
        }
        if (abs(transY * 4) >= height) {
            return false
        }
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        Log.d("BAAA", "onFling 滑动")
        val pointCount1 = e1?.pointerCount ?: 0
        val pointCount2 = e2.pointerCount
        if (pointCount1 > 1 || pointCount2 > 1) {
            return false
        }

        flingRunnable.onFling(scrollX, scrollY, -velocityX.toInt(), -velocityY.toInt())
        postOnAnimation(flingRunnable)
        return false
    }

    inner class FlingRunnable : Runnable {

        private var currentX = 0
        private var currentY = 0

        fun onFling(startX: Int, startY: Int, velocityX: Int, velocityY: Int) {
            currentX = startX
            currentY = startY
            scroller.fling(
                startX,
                startY,
                velocityX,
                velocityY,
                0,
                width,
                0,
                height
            )
        }

        override fun run() {
            if (scroller.isFinished) {
                return
            }
            if (scroller.computeScrollOffset()) {
                val x = scroller.currX
                val y = scroller.currY
                matrix.postTranslate(currentX - x.toFloat(), currentY - y.toFloat())
                invalidate()
                currentX = x
                currentY = y
                postOnAnimation(this)
            }
        }
    }

    private var scale = 1f
    private var lastFactor = 1f
    private var isScale = false

    /**
     * @return true 表示缩放事件被处理了，检测器会重新累计缩放因子， false继续累计缩放因子
     */
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        Log.d(
            "BAAA",
            "onScale 缩放 scaleFactor = ${detector.scaleFactor} scale = $scale focusX = ${detector.focusX} focusY = ${detector.focusY}" // ktlint-disable max-line-length
        )
        scale = detector.scaleFactor / lastFactor
        scale = max(MIN_SCALE, scale)
        scale = min(MAX_SCALE, scale)
        matrix.postScale(scale, scale, detector.focusX, detector.focusY)
        invalidate()
        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        Log.d("BAAA", "onScaleBegin 缩放开始 scaleFactor = ${detector.scaleFactor}")
        isScale = true
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        Log.d("BAAA", "onScaleEnd 缩放结束 scaleFactor = ${detector.scaleFactor}")
        lastFactor = scale
        isScale = false
    }
}
