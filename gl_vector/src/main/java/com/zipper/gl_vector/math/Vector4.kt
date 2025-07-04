package com.zipper.gl_vector.math

@JvmInline
value class Vector4(
    private val values: FloatArray = FloatArray(4)
) {

    init {
        if (values.size != 4) {
            throw IllegalArgumentException("Vector4 values must be 4")
        }
    }

    val x get() = values[0]

    val y get() = values[1]

    val z get() = values[2]

    val w get() = values[3]

    fun values() = values

    fun set(x: Float, y: Float, z: Float, w: Float) = apply {
        values[0] = x
        values[1] = y
        values[2] = z
        values[3] = w
    }
}