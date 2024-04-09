package com.zipper.gldemo2

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent

class MyGLSurfaceView(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs), IGestureCallback {

    private val render = MyGLRender(context)

    private val gestureHandler = GestureHandler(context, this)

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(false)
        setRenderer(render)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureHandler.onTouchEvent(event)
        return true
    }


    override fun onSingleTapUp(x: Float, y: Float) {
        queueEvent {

        }
        render.onSingleTapUp(x, y)
        requestRender()
    }

    override fun onScroll(distanceX: Float, distanceY: Float) {
        queueEvent {

        }
        this.render.onScroll(distanceX / width, distanceY / height)
        requestRender()
    }

    override fun onScaleStart(scale: Float, focusX: Float, focusY: Float) {
        queueEvent {

        }
        this.render.onScaleStart(scale, focusX, focusY)
    }

    override fun onScale(scale: Float, focusX: Float, focusY: Float) {
        queueEvent {

        }
        this.render.onScale(scale)
        requestRender()
    }

    override fun runTaskOnMain(runnable: Runnable) {
        postOnAnimation(runnable)
    }

    override fun cancelTaskOnMain(runnable: Runnable) {
        removeCallbacks(runnable)
    }

}