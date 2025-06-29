package com.zipper.gl_vector.math

import kotlin.math.abs
import kotlin.math.sqrt

class Vector3 : Vector<Vector3> {

    var x: Float = 0f
        private set
    var y: Float = 0f
        private set
    var z: Float = 0f
        private set

    constructor(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(v: Vector3) {
        set(v)
    }

    override fun copy(): Vector3 {
        return Vector3(this)
    }

    override fun length(sqrt: Boolean): Float {
        if (sqrt) {
            return sqrt(x * x + y * y + z * z)
        }
        return x * x + y * y + z * z
    }

    override fun limit(limit: Float): Vector3 {
        val len2 = length(false)
        val limit2 = limit * limit
        if (len2 > limit2) {
            return scale(sqrt(limit2 / len2))
        }
        return this
    }

    override fun clamp(min: Float, max: Float): Vector3 {
        val len2 = length(false)
        if (len2 == 0f) {
            return this
        }
        val max2 = max * max
        if (len2 > max2) {
            return scale(max2 / len2)
        }
        val min2 = min * min
        if (len2 < min2) {
            return scale(min2 / len2)
        }
        return this
    }

    override fun set(v: Vector3): Vector3 = set(v.x, v.y, v.z)

    fun set(x: Float, y: Float, z: Float): Vector3 = apply {
        this.x = x
        this.y = y
        this.z = z
    }

    override fun add(v: Vector3): Vector3 = set(x + v.x, y + v.y, z + v.z)

    override fun sub(v: Vector3): Vector3 = set(x - v.x, y - v.y, z - v.z)

    override fun normalize(): Vector3 = apply {
        val len2 = length(false)
        if (len2 == 0f || len2 == 1f) {
            return@apply
        }
        scale(1f / sqrt(len2))
    }

    override fun dot(v: Vector3): Float {
        return x * v.x + y * v.y + z * v.z
    }

    override fun scale(s: Float): Vector3 = set(x * s, y * s, z * s)

    override fun distance(v: Vector3): Float {
        val dx = x - v.x
        val dy = y - v.y
        val dz = z - v.z

        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    override fun isUnit(margin: Float): Boolean {
        return abs(length(false) - 1f) < margin
    }

    override fun isZero(margin: Float): Boolean {
        return length(false) < margin
    }

    override fun epsilonEquals(v: Vector3, epsilon: Float): Boolean {
        return abs(x - v.x) < epsilon && abs(y - v.y) < epsilon && abs(z - v.z) < epsilon
    }

    override fun toString(): String {
        return "Vector3($x, $y, $z)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector3

        if (x.toIntBits() != other.x.toIntBits()) return false
        if (y.toIntBits() != other.y.toIntBits()) return false
        if (z.toIntBits() != other.z.toIntBits()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.toIntBits().hashCode()
        result = 31 * result + y.toIntBits().hashCode()
        result = 31 * result + z.toIntBits().hashCode()
        return result
    }

}