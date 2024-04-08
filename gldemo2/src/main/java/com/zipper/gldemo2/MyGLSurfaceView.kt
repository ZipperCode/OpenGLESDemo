package com.zipper.gldemo2

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent

class MyGLSurfaceView(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

    private val render = MyGLRender(context)

    private val gestureDetector = GestureDetector(context, MyGestureListener())

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(false)
        setRenderer(render)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            val x = e.x
            val y = e.y

            queueEvent {
                render.onClick(x, y)
                requestRender()
            }
            return super.onDown(e)
        }
    }

}