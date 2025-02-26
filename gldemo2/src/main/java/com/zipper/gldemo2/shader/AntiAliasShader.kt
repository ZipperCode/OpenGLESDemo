package com.zipper.gldemo2.shader

import android.content.Context
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TRIANGLE_STRIP
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniform2i
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import com.zipper.gldemo2.AssetsUtil
import com.zipper.gldemo2.OpenGLHelper

/**
 * 抗锯齿处理
 * @author  zhangzhipeng
 * @date    2025/2/18
 */
class AntiAliasShader(
    private val context: Context
) : BaseShader() {

    private var textureLocation = -1

    private var resolutionLocation = -1

    fun onSurfaceCreate() {
        programHandle[0] = OpenGLHelper.createProgram(getVertexShaderCode(), getFragmentShaderCode())
        positionLocation = getAttribLocation("aPosition")
        coordinateLocation = getAttribLocation("aTextureCoordinate")
        matrixLocation = getUniformLocation("uMatrix")
        textureLocation = getUniformLocation("uTexture")
        resolutionLocation = getUniformLocation("uResolution")
    }

    fun onDrawFrame(textureId: Int, matrix: FloatArray, width: Int, height: Int) {
        glUseProgram(programHandle[0])
        enablePositionVertex()
        enableCoordinateVertex()
        glUniform2i(resolutionLocation, width, height)
        glUniformMatrix4fv(matrixLocation, 1, false, matrix, 0)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glUniform1i(textureLocation, 0)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        disablePositionVertex()
        disableCoordinateVertex()
        glUseProgram(0)
    }

    override fun getVertexShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gles/anti_alias.vert")
    }

    override fun getFragmentShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gles/anti_alias.frag")
    }
}