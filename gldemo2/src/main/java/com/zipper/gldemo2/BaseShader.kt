package com.zipper.gldemo2

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

abstract class BaseShader {
    private val vertexPositions = floatArrayOf(
        -1.0f, 1.0f, // 左下
        -1.0f, -1.0f, // 右下
        1.0f, 1.0f, // 右上
        1.0f, -1.0f, // 左上
    )

    private val textureCoordinate = floatArrayOf(
        0.0f, 0f, // 圆点 0,0
        0.0f, 1.0f, // 0,1
        1.0f, 0f, // 1,0
        1.0f, 1f, // 1,1
    )

    private val positionBuffer: FloatBuffer = getFloatBuffer(vertexPositions)

    private val textureCoordinateBuffer: FloatBuffer = getFloatBuffer(textureCoordinate)

    protected open val programHandle = IntArray(1)

    protected open var positionLocation = -1
    protected open var coordinateLocation = -1


    private fun getFloatBuffer(arr: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(arr.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(arr)
                position(0)
            }
    }

    protected fun getAttribLocation(name: String): Int {
        return GLES20.glGetAttribLocation(programHandle[0], name)
    }

    protected fun getUniformLocation(name: String): Int {
        return GLES20.glGetUniformLocation(programHandle[0], name)
    }

    protected fun enableVertexAttribArray(location: Int) {
        GLES20.glEnableVertexAttribArray(location)
    }

    protected fun enablePositionVertex() {
        positionBuffer.position(0)
        GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 0, positionBuffer)
        GLES20.glEnableVertexAttribArray(positionLocation)
    }

    protected fun disablePositionVertex() {
        GLES20.glDisableVertexAttribArray(positionLocation)
    }

    protected fun enableCoordinateVertex() {
        textureCoordinateBuffer.position(0)
        GLES20.glVertexAttribPointer(coordinateLocation, 2, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer)
        GLES20.glEnableVertexAttribArray(coordinateLocation)
    }

    protected fun disableCoordinateVertex() {
        GLES20.glDisableVertexAttribArray(coordinateLocation)
    }

    protected abstract fun getVertexShaderCode(): String

    protected abstract fun getFragmentShaderCode(): String
}