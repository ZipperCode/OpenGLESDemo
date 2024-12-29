package com.zipper.gldemo2.paint

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView.Renderer
import android.util.Log
import com.zipper.gldemo2.AssetsUtil
import com.zipper.gldemo2.OpenGLHelper
import com.zipper.gldemo2.R
import com.zipper.gldemo2.shader.FrameBufferShader
import com.zipper.gldemo2.shader.MixShader
import com.zipper.gldemo2.shader.TextureShader
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class BrushRenderer(
    private val mBrushView: BrushGLSurfaceView
) : Renderer {

    private var surfaceWidth = 0
    private var surfaceHeight = 0
    private var program = 0

    private var vertexBuffer: FloatBuffer? = null
    private var cacheFrameVertexBuffer: FloatBuffer? = null
    private var cacheFrameVertexCount = 0
    private var vertexCount = 0
    private var brushTextureId = 0
    private var positionLocation = 0
    private var brushTexCoordLocation = 0
    private var brushTexLocation = 0
    private var pointSizeLocation = 0
    private var selectColorLocation = 0


    private var paintFrameBuffer = Framebuffer()

    private var baseFrameBuffer = Framebuffer()
    private var mixColorFrameBuffer = Framebuffer()
    private var resultFrameBuffer = Framebuffer()

    private val cacheFrameBuffer = Framebuffer()

    private val textureShader = TextureShader()
    private val frameTextureShader = FrameBufferShader()
    private val brushPen = BrushPen(mBrushView.context, this)
    private val mixShader = MixShader(mBrushView.context)

    private val selectColorArr: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    private var hasCachedFrame = false

    fun onActionDown(eventPoint: BrushPoint) {
        brushPen.onActionDown(eventPoint)
    }

    fun onActionMove(eventPoint: BrushPoint) {
        brushPen.onActionMove(eventPoint, surfaceWidth, surfaceHeight)
    }

    fun onActionUp(eventPoint: BrushPoint) {
        brushPen.onActionUp(eventPoint, surfaceWidth, surfaceHeight)
    }

    fun drawPoints(points: List<Float>) {
        Log.d("BrushRenderer", "drawPoints count = ${points.size}")
        vertexBuffer = OpenGLHelper.createFloatBuffer(points.toFloatArray())
        vertexCount = points.size / 2
    }

    fun saveFrame(points: List<Float>) {
        Log.d("BrushRenderer", "saveFrame count = ${points.size}")
        cacheFrameVertexBuffer = OpenGLHelper.createFloatBuffer(points.toFloatArray())
        cacheFrameVertexCount = points.size / 2
    }

    fun selectColor(color: Int) {
        brushPen.penColor = color
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val vert = AssetsUtil.getAssetsContent(mBrushView.context, "gles/brush.vert")
        val frag = AssetsUtil.getAssetsContent(mBrushView.context, "gles/brush.frag")
        program = OpenGLHelper.createProgram(vert, frag)
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)

        positionLocation = GLES20.glGetAttribLocation(program, "aPosition")
        GLES20.glEnableVertexAttribArray(positionLocation)
        brushTexCoordLocation = GLES20.glGetAttribLocation(program, "aTexCoord")
        GLES20.glEnableVertexAttribArray(brushTexCoordLocation)
        brushTexLocation = GLES20.glGetUniformLocation(program, "uTexture")
        selectColorLocation = GLES20.glGetUniformLocation(program, "uSelectColor")
        pointSizeLocation = GLES20.glGetUniformLocation(program, "uPointSize")
        brushTexLocation = GLES20.glGetUniformLocation(program, "uTexture")
        textureShader.onSurfaceCreate()
        mixShader.onSurfaceCreate()


        brushTextureId = OpenGLHelper.createTexTexture(
                BitmapFactory.decodeResource(mBrushView.context.resources, R.drawable.brush_paint_normal_128))

//        val brushBitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(brushBitmap)
//        val paint = Paint().apply {
//            setColor(ColorUtils.blendARGB(Color.RED, Color.BLUE, 0.5f))
//        }
//        canvas.drawCircle(64f, 64f, 64f, paint)
//        brushTextureId = OpenGLHelper.createTexTexture(brushBitmap)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
        GLES20.glViewport(0, 0, width, height)
        resultFrameBuffer.init(width, height)
        baseFrameBuffer.init(width, height)
        mixColorFrameBuffer.init(width, height)
        cacheFrameBuffer.init(width, height)
        paintFrameBuffer.init(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(1f, 1f, 1f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        val paintFrameTextureId = paintFrameBuffer.getTextureId()
        val resultFrameTextureId = resultFrameBuffer.getTextureId()
        val cacheFrameTextureId = cacheFrameBuffer.getTextureId()
        vertexBuffer?.run {
            // 画笔绘制的内容
            paintFrameBuffer.withFrame {
                GLES20.glUseProgram(program)
                GLES20.glUniform1f(pointSizeLocation, brushPen.pointSize)
                OpenGLHelper.convertColor(brushPen.penColor, selectColorArr)
                GLES20.glUniform4fv(selectColorLocation, 1, selectColorArr, 0)

                // 绑定纹理
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, brushTextureId)
                GLES20.glUniform1i(brushTexLocation, 0)

                GLES20.glEnable(GLES20.GL_BLEND)
                // 源颜色使用本身，目标颜色取决于源颜色的透明度
//                glBlendFunc(GL_ONE, GL_ONE)
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

                GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, true, 0, vertexBuffer)
                GLES20.glEnableVertexAttribArray(positionLocation)
                GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount)
                GLES20.glDisable(GLES20.GL_BLEND)
            }
            vertexBuffer = null
        }

        // 保存结果
        cacheFrameVertexBuffer?.let {
            cacheFrameBuffer.withFrame {
                textureShader.onDrawFrame(resultFrameTextureId)
                hasCachedFrame = true
            }
            cacheFrameVertexBuffer = null
        }

        if (hasCachedFrame) {
//            var bitmap1: Bitmap? = null
//            var bitmap2: Bitmap? = null
//            cacheFrameBuffer.withFrame {
//                bitmap1 = frameBufferToBitmap()
//            }
//            paintFrameBuffer.withFrame {
//                bitmap2 = frameBufferToBitmap()
//            }
            resultFrameBuffer.withFrame {
                mixShader.onDrawFrame(cacheFrameTextureId, paintFrameTextureId)
            }
        } else {
            resultFrameBuffer.withFrame {
                textureShader.onDrawFrame(paintFrameTextureId)
            }
        }

        textureShader.onDrawFrame(resultFrameTextureId)

    }

    private fun frameBufferToBitmap(): Bitmap {
        val intBuffer = IntBuffer.allocate(surfaceWidth * surfaceHeight)
        intBuffer.position(0)

        GLES20.glReadPixels(0, 0, surfaceWidth, surfaceHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer)

        val bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(intBuffer)

        return bitmap
    }



}