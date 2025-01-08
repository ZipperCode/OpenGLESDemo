package com.opensource.gradientdemo.buffer

import android.opengl.GLES20
import android.opengl.GLES20.GL_STATIC_DRAW
import android.opengl.GLES20.glBindBuffer
import android.opengl.GLES20.glBufferData
import android.opengl.GLES20.glDeleteBuffers
import android.opengl.GLES20.glGenBuffers
import com.opensource.gradientdemo.utils.OpenGLHelper
import java.nio.IntBuffer

/**
 * VBO 缓冲区
 * @author  zhangzhipeng
 * @date    2025/1/8
 */
class VertBuffer {
    private val vertVboId = IntArray(1)

    fun init(vertArray: FloatArray) {
        glGenBuffers(1, IntBuffer.wrap(vertVboId))
        glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertVboId[0])
        glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertArray.size * Float.SIZE_BYTES,
            OpenGLHelper.createFloatBuffer(vertArray),
            GL_STATIC_DRAW // 静态数据，多次绘制保持不变
        )
    }

    fun with(block: () -> Unit) {
        glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertVboId[0])
        block()
        glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    fun release() {
        glDeleteBuffers(1, IntBuffer.wrap(vertVboId))
    }
}