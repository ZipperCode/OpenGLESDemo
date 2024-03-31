package com.zipper.opengl.filter

import android.opengl.GLES20
import com.zipper.opengl.utils.OpenGLHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class RectFilter {
    private val vertexShaderCode =
        """
        attribute vec4 vPosition;
        void main() {
            gl_Position = vPosition;
        } 
        """.trimIndent()
    private val fragmentShaderCode =
        """
        precision mediump float;
        uniform vec4 vColor;
        void main() {
            gl_FragColor = vColor;
        }
        """.trimIndent()

    private val vertexCoords =
        floatArrayOf(
            -0.5f,
            0.5f,
            -0.5f,
            -0.5f,
            0.5f,
            -0.5f,
            0.5f,
            0.5f,
        )

    private val indexs = shortArrayOf(0, 1, 2, 0, 2, 3)

    private val vPosition: FloatBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    private val vIndex: ShortBuffer = ByteBuffer.allocateDirect(indexs.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
    private var program = 0
    private var vPositionHandle = 0
    private var vColorHandle = 0

    private val color = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    init {
        // 放入顶点坐标
    }

    fun onSurfaceCreate() {
        vPosition.put(vertexCoords)
        vPosition.position(0)
        vIndex.put(indexs)
        vIndex.position(0)
        program = OpenGLHelper.createProgram(vertexShaderCode, fragmentShaderCode)
    }

    fun onDrawFrame() {
        vPosition.position(0)
        GLES20.glUseProgram(program)
        vPositionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 4 * 2, vPosition)
        GLES20.glEnableVertexAttribArray(vPositionHandle)

        vColorHandle = GLES20.glGetUniformLocation(program, "vColor")
        GLES20.glUniform4fv(vColorHandle, 0, color, 0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexs.size, GLES20.GL_UNSIGNED_SHORT, vIndex)

        GLES20.glDisableVertexAttribArray(vPositionHandle)
    }
}
