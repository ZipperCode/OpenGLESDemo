package com.zipper.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class ImageGLSurfaceView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs) {

    private val scaleGestureDetector = ScaleGestureDetector(
        context,
        ScaleGestureListyenerImpl(),
    )

    private val render = MyGLSurfaceRender(context)

    init {
        setEGLContextClientVersion(2)
        setRenderer(MyGLSurfaceRender(context))
    }

    class ScaleGestureListyenerImpl : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return scaleGestureDetector.onTouchEvent(event)
    }
}
