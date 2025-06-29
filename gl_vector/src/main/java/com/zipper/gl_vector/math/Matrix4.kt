package com.zipper.gl_vector.math

import android.opengl.Matrix

@JvmInline
value class Matrix4(
    private val values: FloatArray = IDENTIFY.clone()
) {

    companion object {
        val IDENTIFY = floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
        const val M00 = 0
        const val M01 = 1
        const val M02 = 2
        const val M03 = 3
        const val M10 = 4
        const val M11 = 5
        const val M12 = 6
        const val M13 = 7
        const val M20 = 8
        const val M21 = 9
        const val M22 = 10
        const val M23 = 11
        const val M30 = 12
        const val M31 = 13
        const val M32 = 14
        const val M33 = 15
    }

    fun reset() = apply {
        Matrix.setIdentityM(values, 0)
    }

    fun set(matrix: Matrix4) = apply {
        matrix.values.copyInto(values)
    }

    fun scale(sx: Float, sy: Float, sz: Float) = apply {
        Matrix.scaleM(values, 0, sx, sy, sz)
    }

    fun translate(x: Float, y: Float, z: Float) = apply {
        Matrix.translateM(values, 0, x, y, z)
    }

    fun multiplyVec(vec: FloatArray) = apply {
        Matrix.multiplyMV(vec, 0, values, 0, vec, 0)
    }

    fun multiplyV(vec: Quaternion) = apply {
        Matrix.multiplyMV(vec.values(), 0, values, 0, vec.values(), 0)
    }

    fun multiplyMM(lhs: Matrix4, rhs: Matrix4) = apply {
        Matrix.multiplyMM(values, 0, lhs.values, 0, rhs.values, 0)
    }

    fun orthographic(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) = apply {
        Matrix.orthoM(values, 0, left, right, bottom, top, near, far)
    }

    fun invert() = apply {
        Matrix.invertM(values, 0, values, 0)
    }

    fun setLookAt(eye: Vector3, center: Vector3, up: Vector3) = apply {
        Matrix.setLookAtM(values, 0, eye.x, eye.y, eye.z, center.x, center.y, center.z, up.x, up.y, up.z)
//        Matrix.setLookAtM(values, 0, 0f, 0f, 3.8f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
    }

    fun values(): FloatArray = values

}