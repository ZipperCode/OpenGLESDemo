package com.zipper.opengl.utils

import android.opengl.Matrix

/**
 * @author zhangzhipeng
 * @date 2024/4/3
 */
class GestureMatrixHelper {

    private var matrix: FloatArray = floatArrayOf(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    )


    fun getMatrix() = matrix

    fun initMatrix() {
        matrix = floatArrayOf(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        )
    }

    /**
     * 缩放
     */
    fun scaleM(x: Float, y: Float, z: Float) {
        Matrix.scaleM(this.matrix, 0, x, y, z)
    }

    /**
     * 平移
     */
    fun translateM(x: Float, y: Float, z: Float) {
        Matrix.translateM(this.matrix, 0, x, y, z)
    }

    fun multiply(vertexX: Float, vertexY: Float): FloatArray {
        val topLeftRec = floatArrayOf(-1f, 1f, 0f, 1f)
        val bottomRightRec = floatArrayOf(1f, -1f, 0f, 1f)
        // -1，1 -> 1, -1 矩形与矩阵相乘，得到转化后的坐标，并放入数组
        Matrix.multiplyMV(topLeftRec, 0, this.matrix, 0, topLeftRec, 0)
        Matrix.multiplyMV(bottomRightRec, 0, this.matrix, 0, bottomRightRec, 0)

        val topLeftX = topLeftRec[0]
        val bottomRightX = bottomRightRec[0]
        val topLeftY = topLeftRec[1]
        val bottomRightY = bottomRightRec[1]

        val offsetX = vertexX - topLeftX / (bottomRightX - topLeftX)

        val offsetY = vertexY - topLeftY / (bottomRightY - topLeftY)

        return OpenGLHelper.convertGlVertex(offsetX, offsetY)

    }

}
