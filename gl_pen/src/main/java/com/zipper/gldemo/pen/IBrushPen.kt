package com.zipper.gldemo.pen

import com.zipper.gl.base.GLTexture

interface IBrushPen {

    fun initialize()

    fun getBrushTexture(): GLTexture

    fun generatePoints(startX: Float, startY: Float, endX: Float, endY: Float, onPoint: (BrushPoint) -> Unit)

}