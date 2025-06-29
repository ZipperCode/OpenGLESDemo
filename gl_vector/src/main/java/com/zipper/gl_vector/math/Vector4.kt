package com.zipper.gl_vector.math

import kotlin.math.abs
import kotlin.math.sqrt

class Vector4 : Vector<Vector4> {

    var x: Float = 0f
        private set
    var y: Float = 0f
        private set
    var z: Float = 0f
        private set
    var w: Float = 0f
        private set

    constructor(x: Float = 0f, y: Float = 0f, z: Float = 0f, w: Float = 0f) {
        set(x, y, z, w)
    }

    constructor(v: Vector4) {
        set(v)
    }

    override fun copy(): Vector4 = Vector4(this)

    override fun length(sqrt: Boolean): Float {
        return if (sqrt) sqrt(x * x + y * y + z * z + w * w) else x * x + y * y + z * z + w * w
    }

    override fun limit(limit: Float): Vector4 {
        val len2 = length(false)
        if (len2 > limit) {
            scale(sqrt(limit * limit / len2))
        }
        return this
    }

    override fun clamp(min: Float, max: Float): Vector4 {
        val len2 = length(false)
        if (len2 == 0f) {
            return this
        }
        val max2 = max * max
        if (len2 > max2) {
            scale(sqrt(max2 / len2))
        }
        val min2 = min * min
        if (len2 < min2) {
            scale(sqrt(min2 / len2))
        }
        return this
    }

    override fun set(v: Vector4): Vector4 = set(v.x, v.y, v.z, v.w)

    fun set(x: Float, y: Float, z: Float, w: Float): Vector4 = apply {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    override fun add(v: Vector4): Vector4 {
        return set(x + v.x, y + v.y, z + v.z, w + v.w)
    }

    override fun sub(v: Vector4): Vector4 {
        return set(x - v.x, y - v.y, z - v.z, w - v.w)
    }

    override fun normalize(): Vector4 {
        val len2 = length(false)
        if (len2 == 0f || len2 == 1f) {
            return this
        }
        return scale(1f / sqrt(len2))
    }

    override fun dot(v: Vector4): Float {
        return x * v.x + y * v.y + z * v.z + w * v.w
    }

    override fun scale(s: Float): Vector4 {
        return set(x * s, y * s, z * s, w * s)
    }

    override fun distance(v: Vector4): Float {
        val dx = x - v.x
        val dy = y - v.y
        val dz = z - v.z
        val dw = w - v.w
        return sqrt(dx * dx + dy * dy + dz * dz + dw * dw)
    }

    override fun isUnit(margin: Float): Boolean {
        return abs(length(false)) < margin
    }

    override fun isZero(margin: Float): Boolean {
        return length(false) < margin
    }

    override fun epsilonEquals(v: Vector4, epsilon: Float): Boolean {
        return abs(x - v.x) < epsilon && abs(y - v.y) < epsilon && abs(z - v.z) < epsilon && abs(w - v.w) < epsilon
    }
}