package com.zipper.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.zipper.opengl.filter.BackgroundFilter
import com.zipper.opengl.filter.LineFilter
import com.zipper.opengl.filter.OffscreenBufferHelper
import com.zipper.opengl.filter.TextureFilter
import com.zipper.opengl.utils.GestureMatrixHelper
import com.zipper.opengl.utils.MatrixUtil
import com.zipper.opengl.utils.OpenGLHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLSurfaceRender(private val context: Context) : GLSurfaceView.Renderer {

    private val backgroundFilter = BackgroundFilter(context)

    private val lineFilter = LineFilter(context)

    private val offscreenBufferHelper = OffscreenBufferHelper()

    private val textureFilter = TextureFilter(context)

    private val gestureMatrixHelper = GestureMatrixHelper()




    private var matrixState = MatrixState()
    // =====================================
    private var width: Int = 1024

    private var height: Int = 1024
    private var ratio: Float = 1f


    private var viewWidth: Int = 1
    private var viewHeight: Int = 1

    private var viewRatio = 1f

    private var scaleRatio: Float = 1f
    // 缩放比例
    private var transXRatio: Float = 0f
    private var transYRatio: Float = 0f

    private var originVertexX: Float = 0f
    private var originVertexY: Float = 0f


    /**
     * 固定矩阵，表示不做任何处理
     */
    private val fitMatrix = floatArrayOf(
        1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f
    )

    /**
     * 纹理区域变换矩阵
     */
    private val matrix = floatArrayOf(
        1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f
    )

    private val mvpMatrix = FloatArray(16)

    private var scale: Float = 1f

    fun setScale(scale: Float) {
        this.scale = scale
    }

    /**
     * 处理滚动事件
     * @param distanceXRatio 移动距离与视图的比值
     * @param distanceYRatio 移动距离与视图的比值
     */
    fun onScroll(distanceXRatio: Float, distanceYRatio: Float) {
        if (ratio < 1f) {
            this.transXRatio += (distanceXRatio * viewRatio) / ratio * 2 / scaleRatio
            this.transYRatio += distanceYRatio * 2 / scaleRatio
        } else {
            this.transXRatio += (distanceXRatio * viewRatio) / ratio * 4 / scaleRatio
            this.transYRatio += distanceYRatio * 4 / scaleRatio
        }

        if (this.transXRatio > 1f) {
            this.transXRatio = 1f
        }
        if (this.transXRatio < -1f) {
            this.transXRatio = -1f
        }

        if (transYRatio > 1f) {
            this.transYRatio = 1f
        }
        if (transYRatio < -1f) {
            this.transYRatio = -1f
        }



    }


    fun convertScaleAndTrans(vertexX: Float, vertexY: Float): FloatArray {
        this.originVertexX = vertexX
        this.originVertexY = vertexY
        this.gestureMatrixHelper.initMatrix()
        gestureMatrixHelper.scaleM(scaleRatio, scaleRatio, 1.0f)
        gestureMatrixHelper.translateM(vertexX, vertexY, 0.0f)
        return gestureMatrixHelper.multiply(vertexX, vertexY)
    }

    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?
    ) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)

        backgroundFilter.onSurfaceCreate()
        lineFilter.onSurfaceCreate()
        textureFilter.onSurfaceCreate()
    }

    override fun onSurfaceChanged(
        gl: GL10?,
        width: Int,
        height: Int
    ) {
        this.viewWidth = width
        this.viewHeight = height
        this.viewRatio = width / height.toFloat()
        GLES20.glViewport(0, 0, width, height)
        MatrixUtil.handleOrthoM(matrix, width, height)

    }

    private var isDrawBackground = true

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        backgroundFilter.drawBackground()
        if (isDrawBackground) {
            isDrawBackground = false
            offscreenBufferHelper.checkCreateFrameBuffer(width, height)
            offscreenBufferHelper.switchCustomBuffer()
            backgroundFilter.drawWhite()
            offscreenBufferHelper.switchSystemBuffer()
        }

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        textureFilter.draw(matrix, offscreenBufferHelper.getTextureId())
        lineFilter.draw(matrix)
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
