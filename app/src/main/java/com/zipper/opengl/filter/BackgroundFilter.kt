package com.zipper.opengl.filter

import android.content.Context
import android.opengl.GLES20

/**
 *
 * @author zhangzhipeng
 * @date 2024/4/1
 */
class BackgroundFilter(context: Context) : BaseFilter(context) {

    private val backgroundFloatArr: FloatArray = floatArrayOf(1f, 0f, 0f, 1f)
    private val colorFloatArr: FloatArray = floatArrayOf(1f, 1f, 1f, 1f)

    private var colorHandle = 0

    override fun onSurfaceCreate() {
        super.onSurfaceCreate()
        colorHandle = GLES20.glGetUniformLocation(programHandle, "bg_Color")
    }

    private fun draw(colorArr: FloatArray) {
        useProgram(null)
        enablePointer()
        GLES20.glUniform4fv(this.colorHandle, 1, colorArr, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        disablePointer()
        GLES20.glUseProgram(0)
    }

    fun drawBackground() {
        draw(this.backgroundFloatArr)
    }

    fun drawWhite() {
        draw(this.colorFloatArr)
    }

    override fun getVertexShaderCode(): String {
        return """
            attribute vec4 vPosition;
            attribute vec2 vCoordinate;
            uniform mat4 vMatrix;
            void main() {
                gl_Position = vPosition;
            }
        """.trimIndent()
    }

    override fun getFragmentShaderCode(): String {
        return """
            precision mediump float;
            uniform vec4 bg_Color;
            void main() {
                gl_FragColor = vec4(bg_Color.r, bg_Color.g, bg_Color.b, bg_Color.a);
            }
        """.trimIndent()
    }
}
