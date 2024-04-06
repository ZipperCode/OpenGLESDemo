package com.zipper.egl_fbo

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

abstract class BaseShader() {


    /**
     * 顶点坐标数据缓冲区
     */
    protected val positionBuffer: FloatBuffer = createFloatBuffer(vertexPositions)

    /**
     * 纹理坐标数据缓冲区
     */
    protected val textureCoordinateBuffer: FloatBuffer = createFloatBuffer(textureCoordinates)

    protected val programHandle = IntArray(1)

    open fun onSurfaceCreate() {
        programHandle[0] = OpenGLHelper.createProgram(getVertexShaderCode(), getFragmentShaderCode())
    }

    /**
     * 顶点着色器代码
     */
    protected abstract fun getVertexShaderCode(): String

    /**
     * 片元着色器代码
     */
    protected abstract fun getFragmentShaderCode(): String

    companion object {
        /**
         * 顶点坐标 四个点
         */
        val vertexPositions = floatArrayOf(
            -1.0f, 1.0f, // 左下
            -1.0f, -1.0f, // 右下
            1.0f, 1.0f, // 右上
            1.0f, -1.0f, // 左上
        )

        /**
         * 顶点坐标维度
         */
        const val VertexDimension = 2

        /**
         * 纹理坐标 四个点
         */
        val textureCoordinates = floatArrayOf(
            0f, 1f, // 圆点 0,0
            0f, 0f, // 0,1
            1f, 1f, // 1,0
            1f, 0f, // 1,1
        )

        /**
         * 纹理坐标维度
         */
        const val TextureDimension = 2

        fun createFloatBuffer(data: FloatArray): FloatBuffer {
            val buffer = ByteBuffer.allocateDirect(data.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            buffer.put(data)
            buffer.position(0)
            return buffer
        }
    }
}