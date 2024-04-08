package com.zipper.gldemo2.shader

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.util.Log
import com.zipper.gldemo2.AssetsUtil
import com.zipper.gldemo2.OpenGLHelper

class ColoringShader(
    private val context: Context,
) : BaseShader() {

    private var maskTextureLocation = -1
    private var orgTextureLocation = -1

    private var selectColorLocation = -1
    private var selectMaskColorLocation = -1

    private var matrixLocation = -1
    private var selectColorArr = FloatArray(4)
    private var selectMaskColorArr = FloatArray(4)

    fun onSurfaceCreate() {
        programHandle[0] = OpenGLHelper.createProgram(getVertexShaderCode(), getFragmentShaderCode())
        positionLocation = getAttribLocation("aPosition")
        coordinateLocation = getAttribLocation("aTextureCoordinate")
        maskTextureLocation = getUniformLocation("uMaskTexture")
        orgTextureLocation = getUniformLocation("uOrgTexture")
        selectColorLocation = getUniformLocation("uSelectColor")
        selectMaskColorLocation = getUniformLocation("uSelectMaskColor")
        matrixLocation = getUniformLocation("uMatrix")


    }

    fun onDrawFrame(matrix: FloatArray, maskTextureId: Int, orgTextureId: Int, selectColor: Int, selectMaskColor: Int) {
        GLES20.glUseProgram(programHandle[0])

        GLES20.glUniformMatrix4fv(matrixLocation, 1, false, matrix, 0)

        enablePositionVertex()
        enableCoordinateVertex()

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, maskTextureId)
        GLES20.glUniform1i(maskTextureLocation, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, orgTextureId)
        GLES20.glUniform1i(orgTextureLocation, 1)

        convertColor(selectColor, selectColorArr)
        GLES20.glUniform4fv(selectColorLocation, 1, selectColorArr, 0)
        convertColor(selectMaskColor, selectMaskColorArr)
        GLES20.glUniform4fv(selectMaskColorLocation, 1, selectMaskColorArr, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        disablePositionVertex()
        disableCoordinateVertex()
        GLES20.glUseProgram(0)
    }

    private fun convertColor(color: Int, colorArr: FloatArray) {
        if (color == Color.BLACK) {
            colorArr[0] = -0.39215687f
            colorArr[1] = -0.39215687f
            colorArr[2] = -0.39215687f
            colorArr[3] = 1.0f
            return
        }
        colorArr[0] = Color.red(color) / 255.0f
        colorArr[1] = Color.green(color) / 255.0f
        colorArr[2] = Color.blue(color) / 255.0f
        colorArr[3] = Color.alpha(color) / 255.0f
    }

    override fun getVertexShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gles/coloring_shader.vert")
    }

    override fun getFragmentShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gles/coloring_shader.frag")
    }
}