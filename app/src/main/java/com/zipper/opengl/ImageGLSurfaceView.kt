package com.zipper.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class ImageGLSurfaceView
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs) {
        private val render = MyGLSurfaceRender(context)

        init {
            setEGLContextClientVersion(2)
            setRenderer(MyGLSurfaceRender(context))
        }
    }
