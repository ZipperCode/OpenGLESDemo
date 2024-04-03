package com.zipper.opengl

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.abs
import kotlin.math.max

class ImageGLSurfaceView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs) {

    companion object {
        const val SCALE_MAX = 3.0f
        const val SCALE_MIN = 0.7f
    }


    val render = MyGLSurfaceRender(context)

    private val gestureHandler = GestureHandler(this, render)

    init {
        setEGLContextClientVersion(2)
        setRenderer(render)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureHandler.onTouchEvent(event)
    }

}
