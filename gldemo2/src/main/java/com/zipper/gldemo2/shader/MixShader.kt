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
    private var mixRatioLocation = 0
    private var mixRatio: Float = 0.5f  // 默认混合比例为0.5

    fun setMixRatio(ratio: Float) {
        mixRatio = ratio.coerceIn(0f, 1f)  // 确保比例在0-1之间
    }

    fun onSurfaceCreate() {
        programHandle[0] = OpenGLHelper.createProgram(getVertexShaderCode(), getFragmentShaderCode())
        positionLocation = getAttribLocation("aPosition")
        coordinateLocation = getAttribLocation("aCoordinate")
        baseTexLocation = getUniformLocation("uBaseTexture")
        paintTexLocation = getUniformLocation("uPaintTexture")
        mixRatioLocation = getUniformLocation("uMixRatio")
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

        // 设置混合比例
        GLES20.glUniform1f(mixRatioLocation, mixRatio)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

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