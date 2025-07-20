package com.zipper.gldemo.pen.shader

import com.zipper.gl.base.GL
import com.zipper.gl.base.GLColor

class ColorShader : BaseShader() {

    companion object {
        const val VERTEX_SHADER = """
                        precision highp float;
            attribute vec4 aPosition;
            uniform mat4 uMatrix;
            void main() {
                gl_Position = uMatrix * aPosition;
            }
        """

        const val FRAGMENT_SHADER = """
                        precision highp float;
            uniform vec4 uColor;
            void main() {
                gl_FragColor = uColor;
            }
        """
    }


    private val glColor = GLColor()

    fun setColor(color: Int) {
        glColor.setColor(color)
    }

    override fun render(mvpMatrix: FloatArray) {
        super.render(mvpMatrix)
        program.glUniform4fv("uColor", glColor.values())
        GL.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4)
    }

    override fun getVertexShaderCode(): String {
        return VERTEX_SHADER
    }

    override fun getFragmentShaderCode(): String {
        return FRAGMENT_SHADER
    }
}