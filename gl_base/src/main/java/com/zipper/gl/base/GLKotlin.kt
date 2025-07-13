package com.zipper.gl.base

import android.opengl.GLES20
import kotlin.math.sqrt

typealias GL = GLES20

fun Float.normalizeNdcX(): Float {
    return (this - 0.5f) * 2f
}

fun Float.normalizeNdcY(): Float {
    return (-this + 0.5f) * 2f
}

fun normalize(x: Float, min: Float, max: Float): Float {
    return (x - min) / (max - min)
}

fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2))
}

/**
 * ndc 归一化坐标
 */
fun ndcNormalizeX(glX: Float, minR: FloatArray, maxR: FloatArray): Float {
    return normalize(glX, minR[0], maxR[0])
}

/**
 * ndc 归一化坐标
 */
fun ndcNormalizeYRange(glY: Float, minR: FloatArray, maxR: FloatArray): Float {
    return normalize(glY, minR[1], maxR[1])
}