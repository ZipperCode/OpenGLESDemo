package com.zipper.gldemo.pen

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Scroller
import com.zipper.gl.base.GL
import com.zipper.gl.base.OrthographicCamera
import com.zipper.gl.base.distance
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max
import kotlin.math.min

class GLPenView(
    context: Context, attrs: AttributeSet?
) : GLSurfaceView(context, attrs), GLSurfaceView.Renderer,
    ScaleGestureDetector.OnScaleGestureListener,
    GestureDetector.OnGestureListener,
    View.OnTouchListener {
    private val orthographicCamera = OrthographicCamera()

    private val proxyRender = BrushRenderer(context, orthographicCamera)

    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
        setOnTouchListener(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setBrushConfig(config: BrushConfig) {
        queueEvent {
            proxyRender.setBrushConfig(config)
            requestRender()
        }
    }

    fun setBrushSize(size: Float) {
        queueEvent {
            proxyRender.brushSize = size
        }
    }

    fun clean() {
        queueEvent {
            proxyRender.clean()
            requestRender()
        }
    }

    fun undo() {
        queueEvent {
            proxyRender.undo()
            requestRender()
        }
    }

    fun fallback() {
        queueEvent {
            proxyRender.fallback()
            requestRender()
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        proxyRender.onSurfaceCreated(gl, config)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GL.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GL.glClear(GL.GL_COLOR_BUFFER_BIT)
        GL.glViewport(0, 0, width, height)
        orthographicCamera.updateViewport(width, height)
        proxyRender.onSurfaceChanged(gl, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        orthographicCamera.update()
        proxyRender.onDrawFrame(gl)
    }

    private fun onDown(x: Float, y: Float) {
        queueEvent {
            proxyRender.onScroll(x, y, x, y, true)
        }
        requestRender()
    }

    private fun onScroll(dx: Float, dy: Float) {
        orthographicCamera.onScroll(dx, dy)
        requestRender()
    }

    private fun onScroll(startX: Float, startY: Float, endX: Float, endY: Float, isFirstDown: Boolean) {
        val distance = distance(startX, startY, endX, endY)
        //Log.e("BAAA", "startX = $startX startY = $startY endX = $endX endY = $endY distance = $distance")
        queueEvent {
            proxyRender.onScroll(startX, startY, endX, endY, isFirstDown)
        }
        requestRender()
    }

    private fun onScale(scale: Float, focusX: Float, focusY: Float) {
        orthographicCamera.onScale(scale, focusX, focusY)
        requestRender()
    }


    ///////////////////////////////////////////////////////////////////////////

    protected enum class State {
        None,
        Zone,
        Drag,
    }

    protected open var state = State.None

    private val scaleGestureDetector = ScaleGestureDetector(context, this)

    private val gestureDetector = GestureDetector(context, this, Handler(Looper.getMainLooper()))

    private val minScale = 1f

    private val maxScale = 8f

    private var currentScale = 1f


    enum class Action {
        None,
        Down,
        PointDown,
        PointUp,
        Up
    }

    private var currentAction = Action.None

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event ?: return false
        if (event.action == MotionEvent.ACTION_DOWN) {
            currentAction = Action.Down
            state = State.Drag
        } else if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            currentAction = Action.PointDown
            state = State.Zone
        } else if (event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
            state = State.None
            currentAction = Action.PointUp
        } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            currentAction = Action.Up
            state = State.None
        }
        if (currentAction == Action.PointDown) {
            scaleGestureDetector.onTouchEvent(event)
            doubleGestureDetector.onTouchEvent(event)
            return true
        }

        if (currentAction == Action.Down || currentAction == Action.Up) {
            gestureDetector.onTouchEvent(event)
        }

        return true
    }

    // ======================================= [Scale] =======================================  //


    private var isScale = false

    private var focusX = 0f
    private var focusY = 0f

    final override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        isScale = true
        removeCallbacks(flingRunnable)
        focusX = detector.focusX
        focusY = detector.focusY
        onScale(currentScale, focusX, focusY)
        return true
    }

    final override fun onScale(detector: ScaleGestureDetector): Boolean {
        currentScale *= detector.scaleFactor
        currentScale = max(minScale, currentScale)
        currentScale = min(currentScale, maxScale)
        onScale(currentScale, focusX, focusY)
        return true
    }

    final override fun onScaleEnd(detector: ScaleGestureDetector) {
        onScale(currentScale, focusX, focusY)
        isScale = false
    }

    // ======================================= [Down] =======================================  //

    private val doubleGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            isDown = true
            removeCallbacks(flingRunnable)
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // Log.d("BAAA", "onScroll dx = $distanceX dy = $distanceY")
            orthographicCamera.onScroll(distanceX, distanceY)
            requestRender()
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            startX = null
            startY = null
            return true
        }
    }, Handler(Looper.getMainLooper()))

    private var startX: Float? = null
    private var startY: Float? = null

    private var downX: Float? = null
    private var downY: Float? = null

    private var isDown = false

    final override fun onDown(e: MotionEvent): Boolean {
        isDown = true
        removeCallbacks(flingRunnable)
        startX = e.x
        startY = e.y
        downX = e.x
        downY = e.y
        return false
    }

    final override fun onShowPress(e: MotionEvent) = Unit

    final override fun onLongPress(e: MotionEvent) = Unit

    final override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.d("BAAA", "onSingleTapUp x = $x y = $y")
        if (isDown) {
            onDown(e.x, e.y)
            isDown = false
        }
        startX = null
        startY = null
        return false
    }

    // ======================================= [Scroll] =======================================  //

    private var offsetX = 0f
    private var offsetY = 0f

    private val scroller: Scroller = Scroller(context)

    private val flingRunnable = FlingRunnable()

    final override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        val startX = this.startX ?: e1?.x ?: return false
        val startY = this.startY ?: e1?.y ?: return false
        removeCallbacks(flingRunnable)
//         Log.d("BAAA", "onScroll x = ${e1?.x} y = ${e1?.y}")

        val firstScroll = startX == downX && startY == downY
        onScroll(startX, startY, e2.x, e2.y, firstScroll)
        this.startX = e2.x
        this.startY = e2.y
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val pointCount1 = e1?.pointerCount ?: 0
        val pointCount2 = e2.pointerCount
        if (pointCount1 > 1 || pointCount2 > 1) {
            return false
        }
        offsetX = e2.x
        offsetY = e2.y
        val minX = (offsetX - offsetX).toInt()
        val maxX = (offsetX + offsetX).toInt()
        val minY = (offsetY - offsetY).toInt()
        val maxY = (offsetY + offsetY).toInt()
        scroller.fling(
            offsetX.toInt(),
            offsetY.toInt(),
            (velocityX).toInt(),
            (velocityY).toInt(),
            minX,
            maxX,
            minY,
            maxY,
        )
        return false
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private inner class FlingRunnable : Runnable {

        override fun run() {
            if (scroller.isFinished) {
                return
            }

            if (scroller.computeScrollOffset()) {
                val dx = offsetX - scroller.currX * 1.0f
                val dy = offsetY - scroller.currY * 1.0f
                offsetX = scroller.currX * 1.0f
                offsetY = scroller.currY * 1.0f
                onScroll(dx, dy)
                postOnAnimation(flingRunnable)
            }
        }
    }

}


