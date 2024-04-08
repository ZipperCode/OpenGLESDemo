package com.zipper.gldemo2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.sinyee.babybus.kidscolor.egl.FrameBuffer
import com.zipper.gldemo2.shader.ColoringShader
import com.zipper.gldemo2.shader.LineShader
import com.zipper.gldemo2.shader.TextureShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.min

class MyGLRender(
    private val context: Context
) : GLSurfaceView.Renderer {


    private val textureShader = TextureShader()

    private val coloringShader = ColoringShader(context)

    private val lineShader = LineShader(context)

    private val frameBuffer = FrameBuffer()


    private var maskTextureId = -1
    private var orgTextureId = -1

    private var colorStack = DrawColorStack()

    private  var maskBitmap: Bitmap? = null

//    private val mvpMatrix = FloatArray(16)
    private val mvpMatrix = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f)

    private var matrixState: MatrixState = MatrixState()

    private var viewWidth = 0
    private var viewHeight = 0
    private var viewRatio = 0f

    private var renderWidth = 1024
    private var renderHeight = 1024
    private var renderRatio = 1f

    private var renderRect = RectF()

    fun onClick(x: Float, y: Float) {

        val maskBitmap = this.maskBitmap ?: return

        val realX = (x / viewWidth * renderWidth).toInt()
        val realY = (y / viewHeight * renderHeight).toInt()

        Log.d("BAAA", "real = $realX, $realY")

        val color = maskBitmap.getPixel(realX.toInt(), realY.toInt())
        val record = PaintRecord(Color.parseColor("#d63131"), color)
        colorStack.add(record)
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

//        initMatrix()

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        for (paintRecord in colorStack) {
            coloringShader.onDrawFrame(mvpMatrix, maskTextureId, orgTextureId, paintRecord.drawColor, paintRecord.areaColor)
        }
        lineShader.onDrawFrame(mvpMatrix)
    }

    private fun initMatrix() {
//        this.matrixState.translateM(0.1f, 0.1f, 0f)
        this.matrixState.scaleM(-0.1f, 0.1f,0.0f)
        System.arraycopy(this.matrixState.finalMatrix, 0, mvpMatrix, 0, 16)
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