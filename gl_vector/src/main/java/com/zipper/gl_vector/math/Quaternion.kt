package com.zipper.gl_vector.math

/**
 * 四元组
 */
@JvmInline
value class Quaternion(
    private val values: FloatArray = FloatArray(4)
) {
    init {
        if (values.size != 4) {
            throw IllegalArgumentException("Quaternion values must be 4")
        }
    }

    val x: Float get() = values[0]
    val y: Float get() = values[1]

    fun values() = values

    fun set(x: Float, y: Float, z: Float, w: Float): Quaternion {
        values[0] = x
        values[1] = y
        values[2] = z
        values[3] = w
        return this
    }

    fun homogeneous(): Quaternion {
        values[0] = 0f
        values[1] = 0f
        values[2] = 0f
        values[3] = 1f
        return this
    }
}