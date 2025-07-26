package com.zipper.gldemo.pen

import com.zipper.gl.base.normalizeNdcX
import kotlin.math.sqrt
import kotlin.random.Random

data class BrushPoint(
    val x: Float,
    val y: Float,
    val angle: Float = (0..360).random().toFloat()
) {

    fun distance(other: BrushPoint): Float {
        return sqrt(x * other.x + y * other.y)
    }
}