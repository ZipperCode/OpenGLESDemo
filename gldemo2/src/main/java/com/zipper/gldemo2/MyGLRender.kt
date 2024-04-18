package com.zipper.gldemo2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.sinyee.babybus.kidscolor.egl.FrameBuffer
import com.zipper.gldemo2.shader.BackgroundShader
import com.zipper.gldemo2.shader.ColoringShader
import com.zipper.gldemo2.shader.LineShader
import com.zipper.gldemo2.shader.TextureShader
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max
import kotlin.math.min

class MyGLRender(
    private val context: Context
) : GLSurfaceView.Renderer {


    private val textureShader = TextureShader()

    private val coloringShader = ColoringShader(context)

    private val lineShader = LineShader(context)

    private val backgroundShader = BackgroundShader()

    private val frameBuffer = FrameBuffer()


    private var maskTextureId = -1
    private var orgTextureId = -1

    private var colorStack = DrawColorStack()

    private var maskBitmap: Bitmap? = null

    private val identityMatrix = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f)

    private val mvpMatrix = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f)

    private var matrixState: MatrixState = MatrixState()

    private var viewWidth = 0
    private var viewHeight = 0
    private var viewRatio = 0f

    private var renderWidth = 1024
    private var renderHeight = 1024
    private var renderRatio = 1f

    private var renderRect = RectF()

    private var glOffsetX = 0f
    private var glOffsetY = 0f

    private var glScale = 1f

    /**
     * 左上角的顶点向量
     */
    private var topLeftVec = FloatArray(4)

    /**
     * 右下角的顶点向量
     */
    private var bottomRightVec = FloatArray(4)

    private val scaleMatrixHelper = ScaleMatrixHelper()

    /**
     * @param distanceXRatio 在view中移动距离的比例 移动距离 / view宽度
     * @param distanceYRatio 在view中移动距离的比例 移动距离 / view高度
     */
    fun onScroll(distanceXRatio: Float, distanceYRatio: Float) {
        val renderOffsetX = distanceXRatio * viewRatio / renderRatio
        // 渲染高度比宽度大的情况，移动速度为2倍，否则4倍
        val speed = if (renderRatio < 1f) 2f else 4f
        this.glOffsetX += (renderOffsetX * speed) / glScale
        this.glOffsetY += distanceYRatio * speed / glScale
        // 范围控制
        glOffsetX = min(glOffsetX, 1f)
        glOffsetX = max(glOffsetX, -1f)
        glOffsetY = min(glOffsetY, 1f)
        glOffsetY = max(glOffsetY, -1f)

//        Log.d("BAAA", "renderOnScroll distanceXRatio = $distanceXRatio, distanceYRatio = $distanceYRatio")
//        Log.d("BAAA", "renderOnScroll glOffsetX = $glOffsetX glOffsetY = $glOffsetY")
    }

    fun onSingleTapUp(x: Float, y: Float) {
        val glVert = OpenGLHelper.convertGlVertex(x / viewWidth, y / viewHeight)
        val ratioArr = getRenderRatio(glVert[0], glVert[1]) ?: return
//        Log.d("BAAA", "onSingleTapUp ratioArr = ${ratioArr.contentToString()}")
        val maskBitmap = this.maskBitmap ?: return
        var realX = (ratioArr[0] * renderWidth).toInt()
        var realY = (ratioArr[1] * renderHeight).toInt()
        realX = max(realX, 0)
        realX = min(realX, renderWidth - 1)
        realY = max(realY, 0)
        realY = min(realY, renderHeight - 1)
//        Log.i("BAAA", "real  = ${intArrayOf(realX, realY).contentToString()}")
        val color = maskBitmap.getPixel(realX, realY)
//        Log.i("BAAA", "color = ${Integer.toHexString(color)}")
        val record = PaintRecord(Color.RED, color)
        colorStack.add(record)
    }

    private fun getRenderRatio(glVertX: Float, glVertY: Float): FloatArray? {
//        Log.d("BAAA", "topLeftVec     = ${topLeftVec.contentToString()}")
//        Log.d("BAAA", "bottomRightVec = ${bottomRightVec.contentToString()}")
        // 不在渲染位置返回空
        if (glVertX > bottomRightVec[0] || glVertX < topLeftVec[0] || glVertY > topLeftVec[1] || glVertY < bottomRightVec[1]) {
            return null
        }
        // 坐标相对于渲染位置的比例
        val xRatio = (glVertX - topLeftVec[0]) / (bottomRightVec[0] - topLeftVec[0])
        val yRatio = (glVertY - topLeftVec[1]) / (bottomRightVec[1] - topLeftVec[1])

        return floatArrayOf(xRatio, yRatio)
    }

    private var glFocusX: Float = 0f
    private var glFocusY: Float = 0f

    private var glScaleCenterX: Float = 0f
    private var glScaleCenterY: Float = 0f


    fun onScaleStart(scale: Float, focusX: Float, focusY: Float) {
        val focusVert = OpenGLHelper.convertGlVertex(focusX / viewWidth, focusY / viewHeight)
        this.glFocusX = focusVert[0]
        this.glFocusY = focusVert[1]
        val lastMatrix = scaleMatrixHelper.resetMatrix()
            .scaleM(scale, scale, scale)
            .translateM(glOffsetX, glOffsetY, 0f)
            .getMatrix()
        val topLeftVec = floatArrayOf(-1.0f, 1.0f, 0.0f, 1.0f)
        val bottomRightVec = floatArrayOf(1.0f, -1.0f, 0.0f, 1.0f)
        Matrix.multiplyMV(topLeftVec, 0, lastMatrix, 0, topLeftVec, 0)
        Matrix.multiplyMV(bottomRightVec, 0, lastMatrix, 0, bottomRightVec, 0)
        val centerArr = OpenGLHelper.convertGlVertex(
            (glFocusX - topLeftVec[0]) / (bottomRightVec[0] - topLeftVec[0]),
            (glFocusY - topLeftVec[1]) / (bottomRightVec[1] - topLeftVec[1])
        )

        this.glScaleCenterX = centerArr[0]
        this.glScaleCenterY = centerArr[1]

    }

    fun onScale(scale: Float) {

        val matrix = scaleMatrixHelper.resetMatrix()
            .scaleM(scale, scale, scale)
            .translateM(glOffsetX, glOffsetY, 0f)
            .getMatrix()
        val focusVec = floatArrayOf(glScaleCenterX, glScaleCenterY, 0.0f, 1.0f)
        Matrix.multiplyMV(focusVec, 0, matrix, 0, focusVec, 0)
        this.glOffsetX -= (focusVec[0] - this.glFocusX) / scale;
        this.glOffsetY -= (focusVec[1] - this.glFocusY) / scale;
        this.glScale = scale
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        matrixState = MatrixState()
        lineShader.onSurfaceCreate()

        val maskBitmap = AssetsUtil.decodeBitmapFromAssets(context, "903388/mask.png")

        maskTextureId = OpenGLHelper.createTexTexture(maskBitmap)

        maskBitmap?.recycle()

        val bitmap = AssetsUtil.decodeBitmapFromAssets(context, "903388/org.png")
        orgTextureId = OpenGLHelper.createTexTexture(bitmap)
        bitmap?.recycle()
        textureShader.onSurfaceCreate()

        coloringShader.onSurfaceCreate()
        backgroundShader.onSurfaceCreate()

        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Log.d("BAAA", "onSurfaceChanged size = $width, $height")
        handleSurfaceChanged(width, height)
        val bitmap = AssetsUtil.decodeBitmapFromAssets(context, "903388/mask.png")!!
        this.maskBitmap = Bitmap.createScaledBitmap(bitmap, renderWidth, renderHeight, true)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        backgroundShader.onDrawFrame(Color.RED, identityMatrix)
        initMatrix()
        backgroundShader.onDrawFrame(Color.WHITE, mvpMatrix)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        for (paintRecord in colorStack) {
            coloringShader.onDrawFrame(mvpMatrix, maskTextureId, orgTextureId, paintRecord.drawColor, paintRecord.areaColor)
        }
        lineShader.onDrawFrame(mvpMatrix)
    }

    private fun initMatrix() {
        this.matrixState.pushStack()
        this.matrixState.scaleM(this.glScale, this.glScale, this.glScale)
        this.matrixState.translateM(this.glOffsetX, this.glOffsetY, 0f)
        val finalMatrix = this.matrixState.finalMatrix
        System.arraycopy(finalMatrix, 0, mvpMatrix, 0, 16)

        this.topLeftVec = floatArrayOf(-1f, 1f, 0f, 1f)
        // 左边界
        Matrix.multiplyMV(this.topLeftVec, 0, finalMatrix, 0, this.topLeftVec, 0)

        // 右边界
        this.bottomRightVec = floatArrayOf(1f, -1f, 0f, 1f)
        Matrix.multiplyMV(this.bottomRightVec, 0, finalMatrix, 0, this.bottomRightVec, 0)

//        Log.w("BAAA", "topLeftVec     = ${topLeftVec.contentToString()}")
//        Log.w("BAAA", "bottomRightVec = ${bottomRightVec.contentToString()}")
        System.arraycopy(this.matrixState.finalMatrix, 0, mvpMatrix, 0, 16)
        this.matrixState.popStack()
    }

    /**
     * 调整渲染视图和投影矩阵，来保证渲染的内容不变形的绘制在Surface上
     */
    private fun handleSurfaceChanged(width: Int, height: Int) {
        this.viewWidth = width
        this.viewHeight = height
        this.viewRatio = viewWidth.toFloat() / viewHeight.toFloat()
        // 调整正交投影
        if (viewWidth > viewHeight) {
            if (renderRatio > viewRatio) {
                this.matrixState.orthoM(-viewRatio * renderRatio, viewRatio * renderRatio, -1f, 1f, 3f, 7f)
            } else {
                this.matrixState.orthoM(-renderRatio / viewRatio, renderRatio / viewRatio, -1f, 1f, 3f, 7f)
            }
        } else if (renderRatio > viewRatio) {
            this.matrixState.orthoM(-1f, 1f, (-1f / viewRatio) * renderRatio, (1f / viewRatio) * renderRatio, 3f, 7f)
        } else {
            this.matrixState.orthoM(-1f, 1f, (-renderRatio / viewRatio), (renderRatio / viewRatio), 3f, 7f)
        }

        val top = (viewHeight - renderHeight) / 2f
        val left = (viewWidth - renderWidth) / 2f

        renderRect.set(top, left, left + renderWidth.toFloat(), top + renderHeight.toFloat())

        // 调整观察矩阵
        this.matrixState.setLookAtM(0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
    }
}