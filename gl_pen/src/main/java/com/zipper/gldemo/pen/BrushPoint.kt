package com.zipper.gldemo.pen

import com.zipper.gl.base.normalizeNdcX
import kotlin.math.sqrt

data class BrushPoint(
    val x: Float,
    val y: Float,
) {

    fun distance(other: BrushPoint): Float {
        return sqrt(x * other.x + y * other.y)
    }
}