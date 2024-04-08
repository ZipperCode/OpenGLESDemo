package com.zipper.gldemo2.shader

import android.opengl.GLES20
import com.zipper.gldemo2.OpenGLHelper

class TextureShader: BaseShader() {

    private var textureLocation = -1


    fun onSurfaceCreate() {
        programHandle[0] = OpenGLHelper.createProgram(getVertexShaderCode(), getFragmentShaderCode())

        positionLocation = getAttribLocation("aPosition")
        coordinateLocation = getAttribLocation("aTextureCoordinate")
        textureLocation = getUniformLocation("uTexture")
    }


    fun onDrawFrame(textureId: Int) {
        GLES20.glUseProgram(programHandle[0])
        enablePositionVertex()
        enableCoordinateVertex()

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureLocation, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        disablePositionVertex()
        disableCoordinateVertex()

        GLES20.glUseProgram(0)
    }

    override fun getVertexShaderCode(): String {
        return """
            precision highp float;
            attribute vec4 aPosition;
            attribute vec2 aTextureCoordinate;

            varying vec2 vTexCoordinate;

            void main() {
                gl_Position = aPosition;
                vTexCoordinate = aTextureCoordinate;
            }
        """.trimIndent()
    }

    override fun getFragmentShaderCode(): String {
        return """
            precision highp float;

            uniform sampler2D uTexture;
            varying vec2 vTexCoordinate;

            void main() {
                vec4 color = texture2D(uTexture, vTexCoordinate);
                gl_FragColor = color;
            }
        """.trimIndent()
    }

}