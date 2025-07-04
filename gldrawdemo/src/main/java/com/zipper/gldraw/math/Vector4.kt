package com.zipper.gldraw.math

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 四维向量
 * @author  zhangzhipeng
 * @date    2025/6/27
 */
class Vector4 : Vector<Vector4> {

    var x: Float
    var y: Float
    var z: Float
    var w: Float

    constructor(x: Float = 0f, y: Float = 0f, z: Float = 0f, w: Float = 0f) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    constructor(v: Vector4) {
        this.x = v.x
        this.y = v.y
        this.z = v.z
        this.w = v.w
    }

    override fun copy(): Vector4 {
        return Vector4(x, y, z, w)
    }

    override fun length(): Float = x * x + y * y + z * z + w * w

    override fun clamp(min: Float, max: Float): Vector4 {
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

    override fun scale(scalar: Float): Vector4 = scale(scalar, scalar, scalar, scalar)

    fun scale(scaleX: Float, scaleY: Float, scaleZ: Float, scalarW: Float): Vector4 = apply {
        this.x *= scaleX
        this.y *= scaleY
        this.z *= scaleZ
        this.w *= scalarW
    }

    override fun normalize(): Vector4 = apply {
        val len = length()
        if (len == 0f || len == 1f) {
            return@apply
        }
        scale(1.0f / sqrt(len))
    }

    override fun linearInterpolate(target: Vector4, alpha: Float): Vector4 = apply {
        x += alpha + (target.x - x)
        y += alpha + (target.y - y)
        z += alpha + (target.z - z)
        w += alpha + (target.w - w)
    }

    override fun distance(v: Vector4): Float {
        val xd = v.x - x
        val yd = v.y - y
        val zd = v.z - z
        val wd = v.w - w
        return xd * xd + yd * yd + zd * zd + wd * wd
    }

    override fun dot(v: Vector4): Float {
        return x * v.x + y * v.y + z * v.z + w * v.w
    }

    override fun minus(v: Vector4): Vector4 = apply {
        x -= v.x
        y -= v.y
        z -= v.z
        w -= v.w
    }

    override fun plus(v: Vector4): Vector4 = apply {
        x += v.x
        y += v.y
        z += v.z
        w += v.w
    }

    override fun set(v: Vector4): Vector4 = set(v.x, v.y, v.z, v.w)

    fun set(x: Float, y: Float, z: Float, w: Float) = apply {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    override fun epsilonEquals(target: Vector4, epsilon: Float): Boolean {
        return abs(x - target.x) < epsilon && abs(y - target.y) < epsilon && abs(z - target.z) < epsilon && abs(w - target.w) < epsilon
    }

    override fun isUnit(margin: Float): Boolean {
        return abs(length() - 1f) < margin
    }

    override fun isZero(): Boolean {
        return x == 0f && y == 0f && z == 0f && w == 0f
    }

    override fun toString(): String {
        return "($x, $y, $z, $w)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vector4) return false

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false
        if (w != other.w) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        result = 31 * result + w.hashCode()
        return result
    }


}