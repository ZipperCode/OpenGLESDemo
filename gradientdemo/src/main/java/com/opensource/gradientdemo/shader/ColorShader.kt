package com.opensource.gradientdemo.shader

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.GL_STATIC_DRAW
import android.opengl.GLES20.glBindBuffer
import android.opengl.GLES20.glBufferData
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGenBuffers
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import com.opensource.gradientdemo.buffer.VertBuffer
import com.opensource.gradientdemo.utils.AssetsUtil
import com.opensource.gradientdemo.utils.OpenGLHelper
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 *
 * @author  zhangzhipeng
 * @date    2025/1/7
 */
class ColorShader(
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
    private var colorLocation = 0
    private val colorArray = floatArrayOf(0f, 0f, 0f, 0f)

    private var vertVboId =0
    private var vertBuffer = VertBuffer()

    fun onSurfaceCreate() {
        programId = OpenGLHelper.createProgram(getVertexShaderCode(), getFragmentShaderCode())
        positionLocation = glGetAttribLocation(programId, "aPosition")
        colorLocation = glGetUniformLocation(programId, "uColor")

        val vboArray = IntArray(1)
        glGenBuffers(1, IntBuffer.wrap(vboArray))
        glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboArray[0])
        val vertArray = floatArrayOf(
            -1.0f, 1.0f, // 左下
            -1.0f, -1.0f, // 右下
            1.0f, 1.0f, // 右上
            1.0f, -1.0f, // 左上
        )

        glBufferData(GLES20.GL_ARRAY_BUFFER, vertArray.size * Float.SIZE_BYTES, OpenGLHelper.createFloatBuffer(vertArray), GL_STATIC_DRAW)
        vertVboId = vboArray[0]
        vertBuffer.init(vertArray)
    }

    fun onDrawFrame(color: Int) {
        OpenGLHelper.convertColor(color, colorArray)
        glUseProgram(programId)
//        glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
//        glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertVboId)
        vertBuffer.with {
            glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0)
            glEnableVertexAttribArray(positionLocation)
        }
        glUniform4fv(colorLocation, 1, colorArray, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun getVertexShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "color.vert")
    }

    private fun getFragmentShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "color.frag")
    }

}