package com.zipper.gl_vector.math

import java.lang.Float.floatToIntBits
import kotlin.math.abs
import kotlin.math.sqrt

class Vector2 : Vector<Vector2> {

    var x: Float = 0f
        private set
    var y: Float = 0f
        private set

    constructor(x: Float = 0f, y: Float = 0f) {
        this.x = x
        this.y = y
    }

    constructor(v: Vector2) {
        set(v)
    }

    override fun copy(): Vector2 = Vector2(this)

    override fun length(sqrt: Boolean): Float {
        if (sqrt) {
            sqrt(x * x + y * y)
        }
        return x * x + y * y
    }

    override fun limit(limit: Float): Vector2 {
        val len = length(false)
        val limit2 = limit * limit
        if (len > limit2) {
            return scale(sqrt(limit2 / len))
        }
        return this
    }

    override fun clamp(min: Float, max: Float): Vector2 {
        val len = length(false)
        if (len == 0f) return this
        val max2 = max * max
        if (len > max2) {
            return scale(sqrt(max2 / len))
        }
        val min2 = min * min
        if (len < min2) {
            return scale(sqrt(min2 / len))
        }
        return this
    }

    override fun set(v: Vector2): Vector2 = apply {
        x = v.x
        y = v.y
    }

    override fun add(v: Vector2): Vector2 = apply {
        x += v.x
        y += v.y
    }

    override fun sub(v: Vector2): Vector2 = apply {
        x -= v.x
        y -= v.y
    }

    override fun normalize(): Vector2 = apply {
        val len = length()
        if (len != 0f) {
            x /= len
            y /= len
        }
    }

    override fun dot(v: Vector2): Float {
        return x * v.x + y * v.y
    }

    override fun scale(s: Float): Vector2 = apply {
        x *= s
        y *= s
    }

    override fun distance(v: Vector2): Float {
        val xd = v.x - x
        val yd = v.y - y
        return sqrt(xd * xd + yd * yd)
    }

    override fun isUnit(margin: Float): Boolean {
        return abs(length(false) - 1f) < margin
    }

    override fun isZero(margin: Float): Boolean {
        return length(false) < margin
    }

    override fun epsilonEquals(v: Vector2, epsilon: Float): Boolean {
        return abs(v.x - x) < epsilon && abs(v.y - y) < epsilon
    }

    override fun toString(): String {
        return "Vector2($x, $y)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector2

        if (floatToIntBits(x) != floatToIntBits(other.x)) return false
        if (floatToIntBits(y) != floatToIntBits(other.y)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = floatToIntBits(x).hashCode()
        result = 31 * result + floatToIntBits(y).hashCode()
        return result
    }


}