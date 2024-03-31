package com.zipper.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.zipper.opengl.filter.ImageFilter
import com.zipper.opengl.filter.RectFilter
import com.zipper.opengl.filter.TrianglesFilter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLSurfaceRender(private val context: Context) : GLSurfaceView.Renderer {
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

    private val trianglesFilter = TrianglesFilter()

    private val rectFilter = RectFilter()

    private val imageFilter = ImageFilter(context)

    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?,
    ) {
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        imageFilter.onSurfaceCreate()
//        vPosition.put(vVertexCoords)
//        vPosition.position(0)
//        program = OpenGLHelper.createProgram(vertexShaderCode, fragmentShaderCode)
    }

    override fun onSurfaceChanged(
        gl: GL10?,
        width: Int,
        height: Int,
    ) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
//        vPosition.position(0)
//        GLES20.glUseProgram(program)
//        vPositionHandle = GLES20.glGetAttribLocation(program, "vPosition")
//        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 4 * 3, vPosition)
//        GLES20.glEnableVertexAttribArray(vPositionHandle)
//
//        vColorHandle = GLES20.glGetUniformLocation(program, "vColor")
//        GLES20.glUniform4fv(vColorHandle, 0, color, 0)
//
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
        imageFilter.onDrawFrame()
    }
}
