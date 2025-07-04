package com.zipper.gl_vector.shader

import com.zipper.gl_vector.GL
import com.zipper.gl_vector.gl.GLTexture

/**
 *
 * @author  zhangzhipeng
 * @date    2025/7/4
 */
class DrawShader : BaseShader() {

    private var maskTexture: GLTexture? = null

    private val colorArr = FloatArray(4)
    private val maskColorArr = FloatArray(4)

    fun setMaskTexture(texture: GLTexture?) = apply {
        maskTexture = texture
    }

    fun setColor(color: Int) = apply {
        color.convertColor(colorArr)
    }

    override fun initialize() {
        super.initialize()
    }

    fun setMaskColor(maskColor: Int) = apply {
        maskColor.convertColor(maskColorArr)
    }

    override fun render(mvpMatrix: FloatArray) {
        val finalMaskTexture = maskTexture ?: return
        super.render(mvpMatrix)
        finalMaskTexture.activeBind(0)
        program.setUniform1i("uMaskTexture", 0)

        program.glUniform4fv("uColor", colorArr)
        program.glUniform4fv("uMaskColor", colorArr)

        GL.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun Int.convertColor(colorArr: FloatArray) {
        colorArr[0] = (this shr 16 and 0xFF) / 255.0f
        colorArr[1] = (this shr 8 and 0xFF) / 255.0f
        colorArr[2] = (this and 0xFF) / 255.0f
        colorArr[3] = (this ushr 24) / 255.0f
    }

    override fun getVertexShaderCode(): String {
        return """
            attribute vec4 aPosition;
            attribute vec2 aTextureCoordinate;

            uniform mat4 uMatrix;
            varying vec2 vTexCoordinate;

            void main() {
                gl_Position = uMatrix * aPosition;
                vTexCoordinate = aTextureCoordinate;
            }
        """.trimIndent()
    }

    override fun getFragmentShaderCode(): String {
        return """
            uniform sampler2D uMaskTexture;
            uniform vec4 uColor;
            uniform vec4 uMaskColor;

            varying vec2 vTexCoordinate;

            const float threashold = 0.02;

            void main() {
                vec4 maskColor = texture2D(uMaskTexture, vTexCoordinate);
                if (distance(maskColor, uMaskColor) <= threashold) {
                    gl_FragColor = uColor;
                }
            }
        """.trimIndent()
    }
}