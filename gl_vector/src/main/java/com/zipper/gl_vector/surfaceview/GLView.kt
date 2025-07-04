package com.zipper.gl_vector.surfaceview

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Scroller
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max
import kotlin.math.min

class GLView(
    context: Context, attrs: AttributeSet?
) : GLSurfaceView(context, attrs), GLSurfaceView.Renderer,
    ScaleGestureDetector.OnScaleGestureListener,
    GestureDetector.OnGestureListener,
    View.OnTouchListener {

    private val renderAdapters = CopyOnWriteArrayList<RenderAdapter>()

    @Volatile
    private var onCreated = false

    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
        setOnTouchListener(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        onCreated = true
        for (adapter in renderAdapters) {
            adapter.onCreated()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        for (adapter in renderAdapters) {
            adapter.onSizeChanged(width, height)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        for (adapter in renderAdapters) {
            adapter.onRender()
        }
    }

    fun register(adapter: RenderAdapter) {
        renderAdapters.add(adapter)
        if (onCreated) {
            queueEvent {
                adapter.onCreated()
                adapter.onSizeChanged(width, height)
            }
        }
    }

    fun unregister(adapter: RenderAdapter) {
        renderAdapters.remove(adapter)
        queueEvent {
            adapter.onDispose()
        }
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

    private val maxScale = 5f

    private var currentScale = 1f

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

        if (state == State.Drag) {
            gestureDetector.onTouchEvent(event)
        } else if (state == State.Zone) {
            scaleGestureDetector.onTouchEvent(event)
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

    private var isDown = false

    final override fun onDown(e: MotionEvent): Boolean {
        isDown = true
        removeCallbacks(flingRunnable)
        return true
    }

    final override fun onShowPress(e: MotionEvent) = Unit

    final override fun onLongPress(e: MotionEvent) = Unit

    final override fun onSingleTapUp(e: MotionEvent): Boolean {
        if (isDown) {
            onDown(e.x, e.y)
            isDown = false
        }
        return false
    }

    // ======================================= [Scroll] =======================================  //
    private var offsetX = 0f
    private var offsetY = 0f

    private val scroller: Scroller = Scroller(context)

    private val flingRunnable = FlingRunnable()

    final override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        removeCallbacks(flingRunnable)
        onTranslate(distanceX, distanceY)
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


    private fun onDown(x: Float, y: Float) {
        queueEvent {
            for (adapter in renderAdapters) {
                adapter.onDown(x, y)
            }
        }
        requestRender()
    }

    private fun onTranslate(dx: Float, dy: Float) {
        queueEvent {
            for (adapter in renderAdapters) {
                adapter.onTranslate(dx, dy)
            }
        }
        requestRender()
    }

    private fun onScale(scale: Float, focusX: Float, focusY: Float) {
        queueEvent {
            for (adapter in renderAdapters) {
                adapter.onScale(scale, focusX, focusY)
            }
        }
        requestRender()
    }

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
                onTranslate(dx, dy)
                postOnAnimation(flingRunnable)
            }
        }
    }

}