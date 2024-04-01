package com.zipper.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.zipper.opengl.filter.BackgroundFilter
import com.zipper.opengl.filter.LineFilter
import com.zipper.opengl.filter.LineFilter2
import com.zipper.opengl.filter.OffscreenBufferHelper
import com.zipper.opengl.filter.RectFilter
import com.zipper.opengl.filter.TextureFilter
import com.zipper.opengl.filter.TrianglesFilter
import com.zipper.opengl.utils.MatrixHelper
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

    private var width: Int = 1

    private var height: Int = 1

    private val matrix: FloatArray = FloatArray(16)

    private val imageFilter = LineFilter2(context)

    private val backgroundFilter = BackgroundFilter(context)

    private val lineFilter = LineFilter(context)

    private val offscreenBufferHelper = OffscreenBufferHelper()

    private val textureFilter = TextureFilter(context)

    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?,
    ) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)

        backgroundFilter.onSurfaceCreate()
        lineFilter.onSurfaceCreate()
        textureFilter.onSurfaceCreate()
    }

    override fun onSurfaceChanged(
        gl: GL10?,
        width: Int,
        height: Int,
    ) {
        this.width = width
        this.height = height
        GLES20.glViewport(0, 0, width, height)
        MatrixHelper.handleOrthoM(matrix, width, height)
//        backgroundFilter.onSurfaceChanged(matrix, width, height)
//        lineFilter.onSurfaceChanged(matrix, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        backgroundFilter.isDrawViewBackground = false
        backgroundFilter.onDrawFrame()
        if (!offscreenBufferHelper.isInit) {
            offscreenBufferHelper.createFrameBuffer(width, height)
        }

        offscreenBufferHelper.switchCustomBuffer()
        backgroundFilter.isDrawViewBackground = false
        backgroundFilter.onDrawFrame()
        offscreenBufferHelper.switchSystemBuffer()

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        textureFilter.onDrawFrame(matrix)

        lineFilter.onDrawFrame(matrix)
    }
}
