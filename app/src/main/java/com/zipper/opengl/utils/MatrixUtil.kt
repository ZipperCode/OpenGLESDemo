package com.zipper.opengl.utils

import android.opengl.Matrix

/**
 *
 * @author  zhangzhipeng
 * @date    2024/4/1
 */
object MatrixUtil {

    fun handleOrthoM(matrix: FloatArray, width: Int, height: Int) {
        val ratio = if (width > height) width.toFloat() / height.toFloat() else height.toFloat() / width.toFloat()
        if (width > height) {
            Matrix.orthoM(matrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
        } else {
            Matrix.orthoM(matrix, 0, -1f, 1f, -ratio, ratio, -1f, 1f)
        }
    }



}
