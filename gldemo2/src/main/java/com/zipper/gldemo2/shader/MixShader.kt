package com.zipper.gldemo2.shader

import android.content.Context
import android.opengl.GLES20
import com.zipper.gldemo2.AssetsUtil
import com.zipper.gldemo2.OpenGLHelper

class MixShader(
    private val context: Context
): BaseShader() {

    private var baseTexLocation = 0
    private var paintTexLocation = 0

    fun onSurfaceCreate() {
        initProgram()
        baseTexLocation = getUniformLocation("uBaseTexture")
        paintTexLocation = getUniformLocation("uPaintTexture")
    }

    fun onDrawFrame(baseTextureId: Int, paintTextureId: Int) {
        GLES20.glUseProgram(programHandle[0])
        enablePositionVertex()
        enableCoordinateVertex()

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, baseTextureId)
        GLES20.glUniform1i(baseTexLocation, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, paintTextureId)
        GLES20.glUniform1i(paintTexLocation, 1)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        disablePositionVertex()
        disableCoordinateVertex()
        GLES20.glUseProgram(0)
    }

    override fun getVertexShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gles/mix_color.vert")
    }

    override fun getFragmentShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gles/mix_color.frag")
    }
}