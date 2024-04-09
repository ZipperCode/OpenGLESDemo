package com.zipper.gldemo2

import android.opengl.Matrix

/**
 *
 * @author  zhangzhipeng
 * @date    2024/4/9
 */
class ScaleMatrixHelper {

    private val identityMatrix = FloatArray(16)

    fun resetMatrix(): ScaleMatrixHelper {
        Matrix.setIdentityM(identityMatrix, 0)
        return this
    }

    fun getMatrix(): FloatArray {
        return identityMatrix
    }

    fun scaleM(scaleX: Float, scaleY: Float, scaleZ: Float): ScaleMatrixHelper {
        Matrix.scaleM(identityMatrix, 0, scaleX, scaleY, scaleZ)
        return this
    }

    fun translateM(translateX: Float, translateY: Float, translateZ: Float): ScaleMatrixHelper {
        Matrix.translateM(identityMatrix, 0, translateX, translateY, translateZ)
        return this
    }
}