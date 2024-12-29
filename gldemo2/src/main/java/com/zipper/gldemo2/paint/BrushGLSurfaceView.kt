package com.zipper.gldemo2.paint

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

class BrushGLSurfaceView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

    val renderer = BrushRenderer(this)
    private val eventPoint = BrushPoint()

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setColor(color: Int) {
        renderer.selectColor(color)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        eventPoint.set(event.x, event.y)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.onActionDown(eventPoint)
                requestRender()
            }

            MotionEvent.ACTION_MOVE -> {
                renderer.onActionMove(eventPoint)
                requestRender()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                renderer.onActionUp(eventPoint)
                requestRender()
            }
        }
        return true
    }
}