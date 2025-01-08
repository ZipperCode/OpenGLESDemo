package com.opensource.gradientdemo

import android.content.Context
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

/**
 *
 * @author  zhangzhipeng
 * @date    2025/1/6
 */
class GradientGLSurfaceView(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

    private val renderer = GradientRender(context)

    private val colorList = listOf(
        Color.parseColor("#000000"),
        Color.parseColor("#ffffff"),
        Color.parseColor("#ff0000"),
        Color.parseColor("#ffff00"),
        Color.parseColor("#0000ff"),
        Color.parseColor("#0000ff"),
        Color.parseColor("#00ff00"),
        Color.parseColor("#00ffff"),
        Color.parseColor("#ff00ff"),
    )

    var currentColor = Color.RED

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun reset() {
        currentColor = Color.RED
        renderer.reset()
        requestRender()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.onTouchDown(event.x, event.y, currentColor)
            }
            MotionEvent.ACTION_UP -> {
                requestRender()
            }
        }
        return true
    }

}