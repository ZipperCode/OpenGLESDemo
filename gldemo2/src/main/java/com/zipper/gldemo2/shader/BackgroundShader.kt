package com.zipper.gldemo2.shader

import android.opengl.GLES20
import com.zipper.gldemo2.OpenGLHelper

/**
 *
 * @author  zhangzhipeng
 * @date    2024/4/10
 */
class BackgroundShader : BaseShader() {

    private var colorLocation = -1
    private var matrixLocation = -1

    private val colorArr = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

    fun onSurfaceCreate() {
        initProgram()
        colorLocation = getUniformLocation("uColor")
        matrixLocation = getUniformLocation("uMatrix")
    }

    fun onDrawFrame(color: Int, matrix: FloatArray) {
        GLES20.glUseProgram(programHandle[0])
        enablePositionVertex()

        GLES20.glUniformMatrix4fv(matrixLocation, 1, false, matrix, 0)

        OpenGLHelper.convertColor(color, colorArr)

        GLES20.glUniform4fv(colorLocation, 1, colorArr, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        disablePositionVertex()
        GLES20.glUseProgram(0)
    }

    override fun getVertexShaderCode(): String {
        return """
            precision highp float;
            attribute vec4 aPosition;
            uniform mat4 uMatrix;
            void main() {
                gl_Position = uMatrix * aPosition;
            }
        """.trimIndent()
    }

    override fun getFragmentShaderCode(): String {
        return """
            precision highp float;
            uniform vec4 uColor;
            void main() {
                gl_FragColor = uColor;
            }
        """.trimIndent()
    }
}