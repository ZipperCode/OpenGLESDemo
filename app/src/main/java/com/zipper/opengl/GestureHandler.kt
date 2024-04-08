package com.zipper.opengl

import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import kotlinx.coroutines.Runnable
import kotlin.math.max
import kotlin.math.min

/**
 *
 * @author zhangzhipeng
 * @date 2024/4/3
 */
class GestureHandler(
    private val renderView: IView
) : GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {
    private enum class State {
        None,
        Zone,
        Drag,
    }

    private enum class EventState {
        Scroll,
        Fling,
        Scale,
        None,
    }

    private var state = State.None

    private var eventState = EventState.None

    private var currentScale = 1f

    private var minScale = 0.7f

    private var maxScale = 3f

    private var scaleFocusX = 0f
    private var scaleFocusY = 0f

    private val scaleGestureDetector = ScaleGestureDetector(renderView.requireContext(), this)

    private val gestureDetector = GestureDetector(renderView.requireContext(), this)

    private val scaledTouchSlop = ViewConfiguration.get(renderView.requireContext()).scaledTouchSlop

    private val scroller = DragScroller(renderView.requireContext())

    private val flingRunnable = FlingRunnable()

    private val graphicsMatrixArray = FloatArray(9)

    private val graphicsMatrix = Matrix()

    private val viewWidth get() = renderView.viewWidth()

    private val viewHeight get() = renderView.viewHeight()

    private val renderWidth get() = renderView.renderWidth()

    private val renderHeight get() = renderView.renderHeight()

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            state = State.Drag
        } else if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            Log.e("BAAA", "ACTION_POINTER_DOWN")
            state = State.Zone
        }

        if (event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
            state = State.None
        }

        scaleGestureDetector.onTouchEvent(event)


        if (state === State.Zone) {
//            Log.e("BAAA", "Zone")
//            scaleGestureDetector.onTouchEvent(event)
//            return true
        } else if (state === State.Drag) {
            Log.d("BAAA", "Drag  currentScale = ${graphicsMatrixArray.contentToString()}  - $currentScale")
            gestureDetector.onTouchEvent(event)
//            Log.d("BAAA", "state = " + state)
        }


//        if (scaleGestureDetector.isInProgress) {
//            gestureDetector.onTouchEvent(event)
//        }
        return true
    }

    fun onSizeChange(width: Int, height: Int) {
//        rect.set(0f, 0f, width.toFloat(), height.toFloat())
//        graphicsMatrix.mapRect(rect)
    }

    private fun requestRender() {
        val fraction = currentScale - minScale / maxScale - currentScale

        Log.w("BAAA", "Render offsetX = ${ offsetX} offsetY = ${offsetY}")
        Log.w("BAAA", "Render offsetXRatio = ${ offsetX/ currentScale} offsetYRatio = ${offsetY / currentScale}")
        graphicsMatrix.reset()
        graphicsMatrix.postTranslate(offsetX / currentScale, offsetY / currentScale)
        graphicsMatrix.postScale(currentScale, currentScale, scaleFocusX, scaleFocusY)
        graphicsMatrix.getValues(graphicsMatrixArray)
//        invertMatrix.reset()
//        invertMatrix.setValues(graphicsMatrixArray)
//        val invertMatrixArr = FloatArray(9)
//        invertMatrix.getValues(invertMatrixArr)

//        Log.w("BAAA", "Render invertMatrix = ${invertMatrixArr.contentToString()}")
        renderView.requestRender(graphicsMatrixArray)
    }

    private fun getMatrixValue(index:Int):Float {
        graphicsMatrix.getValues(graphicsMatrixArray)
        return graphicsMatrixArray[index]
    }

    private fun getMatrixValue(matrix: Matrix, index: Int):Float {
        val arr = FloatArray(9)
        matrix.getValues(arr)
        return arr[index]
    }

    override fun onDown(e: MotionEvent): Boolean {
        Log.e("EventState", "GestureListenerImpl onDown x = ${e.x} y = ${e.y}")
        eventState = EventState.None
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        Log.e("EventState", "onShowPress 按下")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.e("EventState", "onSingleTapUp 在touch down 后没有滑动（onScroll）又没有长按（onLongPress）的情况下触发")
        return false
    }


    private var offsetX = 0f
    private var offsetY = 0f

    private fun getMinX() = min(0f, (-renderWidth * currentScale + viewWidth / 2))

    private fun getMaxX() = max(0f, (renderWidth * currentScale - viewWidth) / 2)

    private fun getMinY() = min(-renderHeight * currentScale + viewHeight / 2f, 0f)

    private fun getMaxY() = max(0f, (renderHeight * currentScale - viewHeight) / 2)

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float,
    ): Boolean {
        cancelAnimation(flingRunnable)
        Log.e("EventState", "Gesture onScroll 滑动 distanceX = $distanceX distanceY = $distanceY")
        updateTranslate(distanceX, distanceY)
        return false
    }

    private fun setTranslate() {
//        Log.d("BAAA", "maxX = ${getMaxX()} minX = ${getMinX()} maxY = ${getMaxY()} minY = ${getMinY()}")
        val fraction = currentScale - minScale / maxScale - minScale
        offsetX = min(offsetX, getMaxX())
        offsetX = max(offsetX, getMinX())
        offsetY = min(offsetY, getMaxY())
        offsetY = max(offsetY, getMinY())

        val currentMatrix = Matrix()
        currentMatrix.setValues(graphicsMatrixArray)

        val afterMatrix = Matrix()
        afterMatrix.set(currentMatrix)
        currentMatrix.postTranslate(offsetX, offsetY)

        val resultMatrix = Matrix()
        afterMatrix.invert(resultMatrix)
        resultMatrix.postConcat(currentMatrix)
        val arr = FloatArray(9)

//        currentMatrix.getValues(arr)
//        Log.d("BAAA", "current = ${arr.contentToString()}")
//        afterMatrix.getValues(arr)
//        Log.d("BAAA", "after = ${arr.contentToString()}")
//        resultMatrix.getValues(arr)
//        Log.d("BAAA", "result = ${arr.contentToString()}")
//
//        val transX = getMatrixValue(resultMatrix, Matrix.MTRANS_X)
//        val transY = getMatrixValue(resultMatrix, Matrix.MTRANS_Y)
//        Log.d("BAAA", "TransX = $transX TransY = $transY")

//        graphicsMatrix.postTranslate(transX, transY)


//        Log.w("BAAA", "setTranslate matrix = ${graphicsMatrixArray.contentToString()}")
//        val invertMatrixArr = FloatArray(9)
//        invertMatrix.getValues(invertMatrixArr)
//        Log.w("BAAA", "setTranslate invertMatrix = ${invertMatrixArr.contentToString()}")
//        graphicsMatrix.setTranslate(offsetX, offsetY)
//        graphicsMatrix.postScale(currentScale, currentScale)
    }

    override fun onLongPress(e: MotionEvent) = Unit

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float,
    ): Boolean {

        Log.e("EventState", "Gesture onFling 滑动")
        val pointCount1 = e1?.pointerCount ?: 0
        val pointCount2 = e2.pointerCount
        if (pointCount1 > 1 || pointCount2 > 1) {
            return false
        }
        val minX = getMinX().toInt()
        val maxX = getMaxX().toInt()
        val minY = getMinY().toInt()
        val maxY = getMaxY().toInt()
        Log.e("EventState", "Gesture onFling 滑动 minX = $minX maxX = $maxX minY = $minY maxY = $maxY offsetX = $offsetX offsetY = $offsetY")
        scroller.fling(
            offsetX.toInt(), offsetY.toInt(),
            velocityX.toInt(), velocityY.toInt(),
            minX, maxX, minY, maxY
//            0,100,0,100
        )

        postOnAnimation(flingRunnable)
        return false
    }

    fun updateTranslate(distanceX: Float, distanceY: Float) {

        offsetX -= distanceX
        offsetY -= distanceY

        setTranslate()
        requestRender()
    }

    fun postOnAnimation(runnable: Runnable) {
        renderView.startAnimation(runnable)
    }

    private fun cancelAnimation(runnable: Runnable) {
        renderView.cancelAnimation(runnable)
    }


    /**
     * @return true 表示缩放事件被处理了，检测器会重新累计缩放因子， false继续累计缩放因子
     */
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        currentScale *= detector.scaleFactor
        currentScale = max(minScale, currentScale)
        currentScale = min(currentScale, maxScale)
        Log.w("BAAA", "setScale")
        graphicsMatrix.setScale(currentScale, currentScale, detector.focusX, detector.focusY)
        scaleFocusX = detector.focusX
        scaleFocusY = detector.focusY
        Log.e("EventState", "Gesture onScale detector.scaleFactor = ${detector.scaleFactor} scale = $currentScale")
        requestRender()
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        Log.e("EventState", "Gesture onScaleEnd currentScale = ${getMatrixValue(Matrix.MSCALE_X)}")
    }

    inner class FlingRunnable : Runnable {

        override fun run() {
            if (scroller.isFinished) {
                return
            }

            if (scroller.computeScrollOffset()) {
                val dx = offsetX - scroller.currX * 1.0f
                val dy = offsetY - scroller.currY * 1.0f
                updateTranslate(dx, dy)
                postOnAnimation(this)
            }
        }
    }
}
