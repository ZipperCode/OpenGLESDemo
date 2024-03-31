package com.zipper.opengl.filter

import android.opengl.GLES20
import com.zipper.opengl.utils.OpenGLHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TrianglesFilter {
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

    private val vVertexCoords =
        floatArrayOf(
            0f,
            0.5f,
            0.0f, // top
            -0.5f,
            -0.5f,
            0.0f, // bottom left
            0.5f,
            -0.5f,
            0.0f, // bottom right
        )
    private val vPosition: FloatBuffer = ByteBuffer.allocateDirect(vVertexCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    private var vPositionHandle = 0
    private var program = 0

    private var vColorHandle = 0

    private val color = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    init {
    }

    fun onSurfaceCreate() {
        vPosition.put(vVertexCoords)
        vPosition.position(0)
        program = OpenGLHelper.createProgram(vertexShaderCode, fragmentShaderCode)
    }

    fun onDrawFrame() {
        vPosition.position(0)
        GLES20.glUseProgram(program)
        vPositionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 4 * 3, vPosition)
        GLES20.glEnableVertexAttribArray(vPositionHandle)

        vColorHandle = GLES20.glGetUniformLocation(program, "vColor")
        GLES20.glUniform4fv(vColorHandle, 0, color, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
    }
}
