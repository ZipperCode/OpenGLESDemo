package com.opensource.gradientdemo.shader

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1f
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniform2fv
import android.opengl.GLES20.glUniform4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import com.opensource.gradientdemo.GradientPoint
import com.opensource.gradientdemo.utils.AssetsUtil
import com.opensource.gradientdemo.utils.OpenGLHelper
import java.nio.FloatBuffer
import kotlin.math.min

/**
 *
 * @author  zhangzhipeng
 * @date    2025/1/7
 */
class GradientShader(
    private val context: Context
) {
    private val vertexBuffer: FloatBuffer = OpenGLHelper.createFloatBuffer(
        floatArrayOf(
            -1.0f, 1.0f, // 左下
            -1.0f, -1.0f, // 右下
            1.0f, 1.0f, // 右上
            1.0f, -1.0f, // 左上
        )
    )

    private var programId = 0
    private var positionLocation = 0
    private var pointCountLocation = 0

    private val glPointStructs = mutableListOf<GLPointStruct>()

    fun onSurfaceCreate() {
        programId = OpenGLHelper.createProgram(getVertexShaderCode(), getFragmentShaderCode())
        positionLocation = GLES20.glGetAttribLocation(programId, "aPosition")
        pointCountLocation = glGetUniformLocation(programId, "uPointCount")

        val colorLocation = glGetUniformLocation(programId, "uColor")
        val pointLocation = glGetUniformLocation(programId, "uPoint")
        glPointStructs.add(GLPointStruct(colorLocation, pointLocation))

        val rColorLocation = glGetUniformLocation(programId, "uRColor")
        val rPointLocation = glGetUniformLocation(programId, "uRPoint")
        glPointStructs.add(GLPointStruct(rColorLocation, rPointLocation))
        val rColor2Location = glGetUniformLocation(programId, "uRColor2")
        val rPoint2Location = glGetUniformLocation(programId, "uRPoint2")
        glPointStructs.add(GLPointStruct(rColor2Location, rPoint2Location))

    }

    fun onDrawFrame(points: List<GradientPoint>) {
        glUseProgram(programId)
        glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        glEnableVertexAttribArray(positionLocation)
        val count = min(points.size, glPointStructs.size)
        glUniform1i(pointCountLocation, count)
        val newPoints = getLastElements(points)

        for (i in 0 until min(newPoints.size, glPointStructs.size)) {
            val point = newPoints[i]
            val struct = glPointStructs[i]
            OpenGLHelper.convertColor(point.color, struct.colorArray)
            glUniform4fv(struct.colorLocation, 1, struct.colorArray, 0)
            glUniform2fv(struct.pointLocation, 1, point.point, 0)
        }

//        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, 0, 4, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

    }

    fun getLastElements(list: List<GradientPoint>, maxCount: Int = 3): List<GradientPoint> {
        return list.takeLast(maxCount.coerceAtMost(list.size))
    }

    private fun getVertexShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gradient.vert")
    }

    private fun getFragmentShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gradient.frag")
    }


    class GLPointStruct(
        val colorLocation: Int,
        val pointLocation: Int,
        val colorArray: FloatArray = floatArrayOf(0f, 0f, 0f, 0f),
    )
}