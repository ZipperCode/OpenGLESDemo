package com.zipper.gl_vector.surfaceview

interface RenderAdapter : IRender{

    fun onDispose()

    fun onDown(x: Float, y: Float) = Unit

    fun onTranslate(dx: Float, dy: Float) = Unit

    fun onScale(scale: Float, focusX: Float, focusY: Float) = Unit
}