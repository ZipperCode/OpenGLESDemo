package com.zipper.gldemo2.paint

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
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

    // 用于截图的变量
    private var needCapture = false
    private var captureRect: RectF? = null
    private var captureBitmap: Bitmap? = null

    fun requestCapture(rect: RectF) {
        needCapture = true
        captureRect = RectF(rect)
        // 请求重绘以触发onDrawFrame
        mBrushView.requestRender()
    }

    fun getCaptureBitmap(): Bitmap? {
        val bitmap = captureBitmap
        captureBitmap = null
        return bitmap
    }

    private fun captureContent(rect: RectF) {
        val x = rect.left.toInt()
        val y = surfaceHeight - rect.bottom.toInt()
        val width = rect.width().toInt()
        val height = rect.height().toInt()

        val intBuffer = IntBuffer.allocate(width * height)
        intBuffer.position(0)

        // 读取像素
        GLES20.glReadPixels(x, y, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer)

        // 创建位图
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(intBuffer)

        // OpenGL的坐标系与Android的坐标系y轴相反，需要垂直翻转
        val matrix = Matrix()
        matrix.postScale(1f, -1f)
        captureBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(1f, 1f, 1f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        
        val paintFrameTextureId = paintFrameBuffer.getTextureId()
        val resultFrameTextureId = resultFrameBuffer.getTextureId()
        val cacheFrameTextureId = cacheFrameBuffer.getTextureId()
        
        // 1. 先将当前的绘制内容渲染到 paintFrameBuffer
        vertexBuffer?.run {
            paintFrameBuffer.withFrame {
                GLES20.glUseProgram(program)
                GLES20.glUniform1f(pointSizeLocation, brushPen.pointSize)
                OpenGLHelper.convertColor(brushPen.penColor, selectColorArr)
                GLES20.glUniform4fv(selectColorLocation, 1, selectColorArr, 0)

                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, brushTextureId)
                GLES20.glUniform1i(brushTexLocation, 0)

                // 启用混合
                GLES20.glEnable(GLES20.GL_BLEND)
                // 使用预乘alpha的混合模式
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

                GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, true, 0, vertexBuffer)
                GLES20.glEnableVertexAttribArray(positionLocation)
                GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount)
                GLES20.glDisable(GLES20.GL_BLEND)
            }
            vertexBuffer = null
        }

        // 2. 当一笔结束时，将结果保存到缓存中
        cacheFrameVertexBuffer?.let {
            // 将当前绘制的内容(paintFrameBuffer)与历史内容(cacheFrameBuffer)混合
            resultFrameBuffer.withFrame {
                // 启用混合
                GLES20.glEnable(GLES20.GL_BLEND)
                // 使用预乘alpha的混合模式
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
                
                mixShader.onDrawFrame(cacheFrameTextureId, paintFrameTextureId)
                
                GLES20.glDisable(GLES20.GL_BLEND)
            }
            // 将混合结果保存到缓存中
            cacheFrameBuffer.withFrame {
                textureShader.onDrawFrame(resultFrameTextureId)
            }
            // 清空当前绘制缓冲区
            paintFrameBuffer.withFrame {
                GLES20.glClearColor(0f, 0f, 0f, 0f)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            }
            hasCachedFrame = true
            cacheFrameVertexBuffer = null
        }

        // 3. 渲染最终画面
        if (hasCachedFrame) {
            // 如果当前有新的绘制内容，则需要与历史内容混合
            if (vertexCount > 0) {
                resultFrameBuffer.withFrame {
                    // 启用混合
                    GLES20.glEnable(GLES20.GL_BLEND)
                    // 使用预乘alpha的混合模式
                    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
                    
                    mixShader.onDrawFrame(cacheFrameTextureId, paintFrameTextureId)
                    
                    GLES20.glDisable(GLES20.GL_BLEND)
                }
            } else {
                // 否则直接显示历史内容
                resultFrameBuffer.withFrame {
                    textureShader.onDrawFrame(cacheFrameTextureId)
                }
            }
        } else {
            // 第一次绘制，直接显示当前绘制内容
            resultFrameBuffer.withFrame {
                textureShader.onDrawFrame(paintFrameTextureId)
            }
        }

        // 4. 显示到屏幕
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        textureShader.onDrawFrame(resultFrameTextureId)
        GLES20.glDisable(GLES20.GL_BLEND)

        // 5. 如果需要截图，在渲染完成后进行截图
        if (needCapture) {
            captureRect?.let { rect ->
                captureContent(rect)
            }
            needCapture = false
            captureRect = null
        }
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

    fun setPenColor(color: Int) {
        brushPen.penColor = color
    }

    /**
     * 设置颜色混合比例
     * @param ratio 混合比例，范围0-1
     *              0表示完全使用历史颜色
     *              1表示完全使用新绘制的颜色
     *              0.5表示两种颜色均匀混合
     */
    fun setMixRatio(ratio: Float) {
        mixShader.setMixRatio(ratio)
    }

    private fun frameBufferToBitmap(): Bitmap {
        val intBuffer = IntBuffer.allocate(surfaceWidth * surfaceHeight)
        intBuffer.position(0)

        GLES20.glReadPixels(0, 0, surfaceWidth, surfaceHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer)

        val bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(intBuffer)

        return bitmap
    }

    /**
     * 获取指定区域的内容
     */
    fun getContentBitmap(rect: RectF): Bitmap {
        // 确保在GL线程中执行
        var resultBitmap: Bitmap? = null
        mBrushView.queueEvent {
            val x = rect.left.toInt()
            val y = surfaceHeight - rect.bottom.toInt()
            val width = rect.width().toInt()
            val height = rect.height().toInt()

            // 绑定帧缓冲区
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, resultFrameBuffer.getTextureId())

            val intBuffer = IntBuffer.allocate(width * height)
            intBuffer.position(0)

            // 读取像素
            GLES20.glReadPixels(x, y, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer)

            // 创建位图
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(intBuffer)

            // OpenGL的坐标系与Android的坐标系y轴相反，需要垂直翻转
            val matrix = Matrix()
            matrix.postScale(1f, -1f)
            resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)

            // 解绑帧缓冲区
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        }

        // 等待GL线程执行完成
        mBrushView.queueEvent {
            synchronized(this) {
                (this as Object).notify()
            }
        }
        synchronized(this) {
            try {
                (this as Object).wait()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        return resultBitmap ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }
}