package com.zipper.gldraw.math

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 二维向量
 * @author  zhangzhipeng
 * @date    2025/6/27
 */
class Vector2 : Vector<Vector2> {

    var x: Float
    var y: Float

    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    constructor(v: Vector2) {
        this.x = v.x
        this.y = v.y
    }

    constructor() {
        x = 0f
        y = 0f
    }

    override fun copy(): Vector2 {
        return Vector2(x, y)
    }

    override fun length(): Float = x * x + y * y

    override fun clamp(min: Float, max: Float): Vector2 {
        val len = length()
        if (len == 0f) {
            return this
        }
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

    override fun scale(scalar: Float): Vector2 = apply {
        this.x *= scalar
        this.y *= scalar
    }

    override fun scale(scaleX: Float, scaleY: Float): Vector2 = apply {
        this.x *= scaleX
        this.y *= scaleY
    }

    override fun normalize(): Vector2 = apply {
        val len = length()
        if (len != 0f) {
            x /= len
            y /= len
        }
    }

    override fun linearInterpolate(target: Vector2, alpha: Float): Vector2 = apply {
        val invAlpha = 1.0f - alpha
        x = x * invAlpha + target.x * alpha
        y = y * invAlpha + target.y * alpha
    }

    override fun distance(v: Vector2): Float {
        val xd = v.x - x
        val yd = v.y - y
        return xd * xd + yd * yd
    }

    override fun dot(v: Vector2): Float {
        return x * v.x + y * v.y
    }

    override fun minus(v: Vector2): Vector2 = apply {
        x -= v.x
        y -= v.y
    }

    override fun plus(v: Vector2): Vector2 = apply {
        x += v.x
        y += v.y
    }

    override fun set(v: Vector2): Vector2 = apply {
        x = v.x
        y = v.y
    }

    override fun set(x: Float, y: Float): Vector2 = apply {
        this.x = x
        this.y = y
    }

    override fun epsilonEquals(target: Vector2, epsilon: Float): Boolean {
        return (abs(x - target.x) < epsilon && abs(y - target.y) < epsilon)
    }

    override fun isUnit(margin: Float): Boolean {
        return abs(length() - 1f) < margin
    }

    override fun isZero(): Boolean {
        return x == 0f && y == 0f
    }

    override fun toString(): String {
        return "($x,$y)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vector2) return false

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }


}