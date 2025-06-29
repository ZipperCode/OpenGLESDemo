package com.zipper.gl_vector

interface RenderAdapter {

    fun onCreate()

    fun onSize(width: Int, height: Int)

    fun onRender()

    fun onDispose()

    fun onDown(x: Float, y: Float) = Unit

    fun onTranslate(dx: Float, dy: Float) = Unit

    fun onScale(scale: Float, focusX: Float, focusY: Float) = Unit
}