package com.zipper.opengl.filter

import android.content.Context
import android.opengl.GLES20

/**
 *
 * @author zhangzhipeng
 * @date 2024/4/1
 */
class TextureFilter(context: Context) : BaseFilter(context) {

    private var fboTextureHandle = 0

    private var renderTextureId = 0

    override fun onSurfaceCreate() {
        super.onSurfaceCreate()
        fboTextureHandle = GLES20.glGetUniformLocation(programHandle, "fTextureWhite")
    }

    fun setTextureIdAndDraw(textureId: Int, matrix: FloatArray?) {
        this.renderTextureId = textureId
        onDrawFrame(matrix)
    }

    override fun onDrawBefore(matrix: FloatArray?) {
        super.onDrawBefore(matrix)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureId)
        GLES20.glUniform1i(fboTextureHandle, 0)
    }

    override fun onDrawAfter() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        super.onDrawAfter()
    }

    override fun getVertexShaderCode(): String {
        return """
attribute vec4 vPosition;
attribute vec2 vCoordinate;
varying vec2 aCoordinate;
uniform mat4 vMatrix;
varying vec2 aFBOTextureCoord;

void main() {
    gl_Position = vMatrix * vPosition;
    aCoordinate = vCoordinate;
    aFBOTextureCoord = vec2(vCoordinate.x, 1.0 - vCoordinate.y);
}

        """.trimIndent()
    }

    override fun getFragmentShaderCode(): String {
        return """
precision mediump float;
varying vec2 aCoordinate;
varying vec2 aFBOTextureCoord;
uniform sampler2D fTextureWhite;

void main() {
    gl_FragColor = texture2D(fTextureWhite, aFBOTextureCoord);
}

        """.trimIndent()
    }
}
