package com.zipper.opengl

import android.R.attr
import android.content.Context
import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.zipper.opengl.filter.BackgroundFilter
import com.zipper.opengl.filter.LineFilter
import com.zipper.opengl.filter.OffscreenBufferHelper
import com.zipper.opengl.filter.TextureFilter
import com.zipper.opengl.utils.GestureMatrixHelper
import com.zipper.opengl.utils.MatrixUtil
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyGLSurfaceRender(private val context: Context) : GLSurfaceView.Renderer {
    private val backgroundFilter = BackgroundFilter(context)

    private val lineFilter = LineFilter(context)

    private val offscreenBufferHelper = OffscreenBufferHelper()

    private val textureFilter = TextureFilter(context)

    private val gestureMatrixHelper = GestureMatrixHelper()

    private var matrixState = MatrixState()



    /**
     * 固定矩阵，表示不做任何处理
     */
    private val fitMatrix = floatArrayOf(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f,
    )

    /**
     * 纹理区域变换矩阵
     */
    private val matrix = floatArrayOf(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f,
    )

    // =====================================

    private val viewRect = RectF()
    private val drawRect = RectF().apply {
        set(0f,0f,1024f, 1024f)
    }

    private var ratio: Float = 1f

    private var viewRatio = 1f

    private var scaleRatio: Float = 1f

    // 缩放比例
    private var transXRatio: Float = 0f
    private var transYRatio: Float = 0f

    private var originVertexX: Float = 0f
    private var originVertexY: Float = 0f

    private var scale: Float = 1f

    // --------

    private val projectMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val mvpMatrix = FloatArray(16)

    private var textureX = 0f
    private var textureY = 0f
    private var textureScale = 1f



    // ------


    fun setScale(scale: Float) {
        this.scale = scale
    }

    private fun getOpenGLCoord(x: Float, y: Float): FloatArray {
        // Convert touch coordinates to OpenGL coordinates
        val touchPoint = floatArrayOf(x, y, 0f, 1f)

        // Get the projection and view matrices
        val pvMatrix = FloatArray(16)
        Matrix.multiplyMM(pvMatrix, 0, projectMatrix, 0, viewMatrix, 0)
        val invertedMatrix = FloatArray(16)
        Matrix.invertM(invertedMatrix, 0, pvMatrix, 0)

        // Transform the touch coordinates to OpenGL coordinates
        val openGLCoord = FloatArray(4)
        Matrix.multiplyMV(openGLCoord, 0, invertedMatrix, 0, touchPoint, 0)
        return openGLCoord
    }

    fun moveTexture(
        distanceX: Float,
        distanceY: Float,
    ) {
        val dxRatio = distanceX / viewRect.width()
        val dyRatio = distanceY / viewRect.height()

        val rx = distanceX * drawRect.width() / viewRect.height()
        val ry = distanceY * drawRect.height() / viewRect.height()

        textureX -= dxRatio
        textureY += dyRatio

        Log.d("BAAA", "offsetX = $textureX offsetY = $textureY rx = $rx ry = $ry")
    }

    fun startFling(velocityX: Float, velocityY: Float) {

    }

    fun convertScaleAndTrans(
        vertexX: Float,
        vertexY: Float,
    ): FloatArray {
        this.originVertexX = vertexX
        this.originVertexY = vertexY
        this.gestureMatrixHelper.initMatrix()
        gestureMatrixHelper.scaleM(scaleRatio, scaleRatio, 1.0f)
        gestureMatrixHelper.translateM(vertexX, vertexY, 0.0f)
        return gestureMatrixHelper.multiply(vertexX, vertexY)
    }

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
        this.viewRect.set(0f, 0f, width.toFloat(), height.toFloat())
        this.viewRatio = width / height.toFloat()
        GLES20.glViewport(0, 0, width, height)
//        MatrixUtil.handleOrthoM(matrix, width, height)
//        MatrixUtil.handleOrthoM(projectMatrix, width, height)
//        Matrix.setIdentityM(modelMatrix, 0)
        val aspectRatio = attr.width.toFloat() / attr.height
        Matrix.frustumM(projectMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 3f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 5f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    private var isDrawBackground = true

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        backgroundFilter.drawBackground()
//        if (isDrawBackground) {
//            isDrawBackground = false
//            offscreenBufferHelper.checkCreateFrameBuffer(drawRect.width().toInt(), drawRect.height().toInt())
//            offscreenBufferHelper.switchCustomBuffer()
//            backgroundFilter.drawWhite()
//            offscreenBufferHelper.switchSystemBuffer()
//        }
//
//        GLES20.glEnable(GLES20.GL_BLEND)
//        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
//        textureFilter.draw(matrix, offscreenBufferHelper.getTextureId())
//        lineFilter.draw(matrix)

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, textureX, textureY, 0f);

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, mvpMatrix, 0);
        lineFilter.draw(mvpMatrix)
    }

    private fun handleMatrix() {
        this.matrixState.pushStack()
        matrixState.scaleM(scaleRatio, scaleRatio, 1.0f)
        matrixState.translateM(transXRatio, transYRatio, 0.0f)
        val finalMatrix = matrixState.finalMatrix
        val topLeftVec = floatArrayOf(-1f, 1f, 0f, 1f)
        val bottomRightVec = floatArrayOf(1f, -1f, 0f, 1f)
        Matrix.multiplyMV(topLeftVec, 0, finalMatrix, 0, topLeftVec, 0)
        Matrix.multiplyMV(bottomRightVec, 0, finalMatrix, 0, bottomRightVec, 0)
        this.matrixState.popStack()
    }
}
