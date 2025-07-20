package com.zipper.gl.base

import android.graphics.Color

@JvmInline
value class GLColor(
    private val values: FloatArray = FloatArray(4)
) {

    val r get() = values[0]
    val g get() = values[1]
    val b get() = values[2]

    init {
        assert(values.size == 4)
    }

    fun values() = values

    fun setColor(color: Int) {
        values[0] = Color.red(color) / 255.0f
        values[1] = Color.green(color) / 255.0f
        values[2] = Color.blue(color) / 255.0f
        values[3] = Color.alpha(color) / 255.0f
    }

    fun upload(loc: Int) {
        GL.glUniform4fv(loc, 1, values, 0)
    }

    fun toColor(): Int {
        return Color.argb(
            (values[3] * 255).toInt() and 0xFF,
            (values[0] * 255).toInt() and 0xFF,
            (values[1] * 255).toInt() and 0xFF,
            (values[2] * 255).toInt() and 0xFF
        )
    }
}