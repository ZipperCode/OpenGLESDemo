package com.zipper.gl_vector.math

class Matrix3 {
    companion object {
        val IDENTIFY = floatArrayOf(
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
        )

        const val M00 = 0
        const val M01 = 1
        const val M02 = 2
        const val M10 = 3
        const val M11 = 4
        const val M12 = 5
        const val M20 = 6
        const val M21 = 7
        const val M22 = 8

        /**
         * 矩阵相乘
         * multi(A, B) => A := AB
         */
        fun multi(left: FloatArray, right: FloatArray) {

            val v00 = left[M00] * right[M00] + left[M01] * right[M10] + left[M02] * right[M20]
            val v01 = left[M00] * right[M01] + left[M01] * right[M11] + left[M02] * right[M21]
            val v02 = left[M00] * right[M02] + left[M01] * right[M12] + left[M02] * right[M22]

            val v10 = left[M10] * right[M00] + left[M11] * right[M10] + left[M12] * right[M20]
            val v11 = left[M10] * right[M01] + left[M11] * right[M11] + left[M12] * right[M21]
            val v12 = left[M10] * right[M02] + left[M11] * right[M12] + left[M12] * right[M22]

            val v20 = left[M20] * right[M00] + left[M21] * right[M10] + left[M22] * right[M20]
            val v21 = left[M20] * right[M01] + left[M21] * right[M11] + left[M22] * right[M21]
            val v22 = left[M20] * right[M02] + left[M21] * right[M12] + left[M22] * right[M22]

            left[M00] = v00
            left[M01] = v01
            left[M02] = v02
            left[M10] = v10
            left[M11] = v11
            left[M12] = v12
            left[M20] = v20
            left[M21] = v21
            left[M22] = v22
        }
    }

    private val value = FloatArray(9)

    constructor(values: FloatArray = IDENTIFY) {
        System.arraycopy(values, 0, value, 0, 9)
    }

    /**
     * 矩阵相乘，并将结果保存在当前矩阵中
     *
     * @param m 矩阵
     * @param leftMulti 是否左乘
     *  true: this = m * this
     *  false: this = this * m
     *
     */
    fun multi(m: Matrix3, leftMulti: Boolean = false): Matrix3 {
        val left = if (leftMulti) m.value else value
        val right = if (leftMulti) value else m.value
        multi(left, right)
        return this
    }



    /**
     * 将矩阵平移
     * @param x x平移
     * @param y y平移
     */
    fun translate(x: Float, y: Float): Matrix3 {
        value[M02] = x
        value[M12] = y
        return this
    }

    fun translate(v: Vector2): Matrix3 {
        return translate(v.x, v.y)
    }

    /**
     * 缩放矩阵
     */
    fun scale(sx: Float, sy: Float): Matrix3 {
        value[M00] *= sx
        value[M11] *= sy
        return this
    }

    fun scale(v: Vector2): Matrix3 {
        return scale(v.x, v.y)
    }


    /**
     * 翻转矩阵
     * 1、计算行列式，如果为0，不可反转
     * 2、求伴随矩阵
     * 3、求逆矩阵，伴随矩阵 / 行列式
     */
    fun invert(): Matrix3 {
        val det = det()
        if (det == 0f) {
            // 不可变换
            return this
        }
        // 除法变成乘法
        val invDet = 1f / det
        // 伴随矩阵
        val v00: Float = value[M11] * value[M22] - value[M21] * value[M12]
        val v10: Float = value[M20] * value[M12] - value[M10] * value[M22]
        val v20: Float = value[M10] * value[M21] - value[M20] * value[M11]
        val v01: Float = value[M21] * value[M02] - value[M01] * value[M22]
        val v11: Float = value[M00] * value[M22] - value[M20] * value[M02]
        val v21: Float = value[M20] * value[M01] - value[M00] * value[M21]
        val v02: Float = value[M01] * value[M12] - value[M11] * value[M02]
        val v12: Float = value[M10] * value[M02] - value[M00] * value[M12]
        val v22: Float = value[M00] * value[M11] - value[M10] * value[M01]

        // 逆矩阵
        value[M00] = invDet * v00
        value[M10] = invDet * v10
        value[M20] = invDet * v20
        value[M01] = invDet * v01
        value[M11] = invDet * v11
        value[M21] = invDet * v21
        value[M02] = invDet * v02
        value[M12] = invDet * v12
        value[M22] = invDet * v22
        return this
    }

    /**
     * Determinant 计算矩阵的行列式
     * 用来判断矩阵是否可逆、求解线性方程组、计算向量叉积、表示变换的缩放因子等。
     * 计算公式： det(A)=(A00 * A11* A22) + (A01 * A12 * A20) + (A02 * A10 * A21) - (A02 * A11 * A20) - (A01 * A10 * A22) - (A02 * A11 * A20)
     */
    fun det(): Float {
        return value[M00] * value[M11] * value[M22] + value[M01] * value[M12] * value[M20] + value[M02] * value[M10] * value[M21]
        -value[M02] * value[M11] * value[M20] - value[M01] * value[M10] * value[M22] - value[M02] * value[M11] * value[M20]
    }

    /**
     * 转置矩阵
     */
    fun transpose(): Matrix3 {
        val v01 = value[M10]
        val v02 = value[M20]
        val v10 = value[M01]
        val v12 = value[M21]
        val v20 = value[M02]
        val v21 = value[M12]
        value[M01] = v01
        value[M02] = v02
        value[M10] = v10
        value[M12] = v12
        value[M20] = v20
        value[M21] = v21
        return this
    }

    override fun toString(): String {
        return """
[${value[M00]} | ${value[M01]} | ${value[M02]}]
[${value[M10]} | ${value[M11]} | ${value[M12]}]
[${value[M20]} | ${value[M21]} | ${value[M22]}]
        """.trimIndent()
    }
}