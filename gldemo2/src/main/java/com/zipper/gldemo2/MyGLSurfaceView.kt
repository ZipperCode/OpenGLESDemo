package com.zipper.gldemo2

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class MyGLSurfaceView(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

    private val render = MyGLRender(context)

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(false)
        setRenderer(render)
    }
}