package com.zipper.gldraw.math

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 三维向量
 * @author  zhangzhipeng
 * @date    2025/6/27
 */
class Vector3 : Vector<Vector3> {

    var x: Float
    var y: Float
    var z: Float

    constructor(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(v: Vector3) {
        this.x = v.x
        this.y = v.y
        this.z = v.z
    }

    override fun copy(): Vector3 {
        return Vector3(x, y, z)
    }

    override fun length(): Float = x * x + y * y + z * z

    override fun clamp(min: Float, max: Float): Vector3 {
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

    override fun scale(scalar: Float): Vector3 = scale(scalar, scalar, scalar)

    fun scale(scaleX: Float, scaleY: Float, scaleZ: Float): Vector3 = apply {
        this.x *= scaleX
        this.y *= scaleY
        this.z *= scaleZ
    }

    override fun normalize(): Vector3 = apply {
        val len = length()
        if (len == 0f || len == 1f) {
            return@apply
        }
        scale(1.0f / sqrt(len))
    }

    override fun linearInterpolate(target: Vector3, alpha: Float): Vector3 = apply {
        x += alpha * (target.x - x)
        y += alpha * (target.y - y)
        z += alpha * (target.z - z)
    }

    override fun distance(v: Vector3): Float {
        val xd = v.x - x
        val yd = v.y - y
        val zd = v.z - z
        return xd * xd + yd * yd + zd * zd
    }

    override fun dot(v: Vector3): Float {
        return x * v.x + y * v.y + z * v.z
    }

    override fun minus(v: Vector3): Vector3 = apply {
        x -= v.x
        y -= v.y
        z -= v.z
    }

    override fun plus(v: Vector3): Vector3 = apply {
        x += v.x
        y += v.y
        z += v.z
    }

    override fun set(v: Vector3): Vector3 = set(v.x, v.y, v.z)

    fun set(x: Float, y: Float, z: Float) = apply {
        this.x = x
        this.y = y
        this.z = z
    }

    override fun epsilonEquals(target: Vector3, epsilon: Float): Boolean {
        return abs(x - target.x) < epsilon && abs(y - target.y) < epsilon && abs(z - target.z) < epsilon
    }

    override fun isUnit(margin: Float): Boolean {
        return abs(length() - 1f) < margin
    }

    override fun isZero(): Boolean {
        return x == 0f && y == 0f && z == 0f
    }

    override fun toString(): String {
        return "($x, $y, $z)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vector3) return false

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }


}