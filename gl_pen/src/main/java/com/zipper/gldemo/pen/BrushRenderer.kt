package com.zipper.gldemo.pen

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLSurfaceView
import android.util.Log
import com.zipper.gl.base.GL
import com.zipper.gl.base.GLColor
import com.zipper.gl.base.GLFrameBuffer
import com.zipper.gl.base.OrthographicCamera
import com.zipper.gldemo.pen.shader.BrushShader
import com.zipper.gldemo.pen.shader.ColorShader
import com.zipper.gldemo.pen.shader.TextureShader
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.hypot

class BrushRenderer(
    private val context: Context,
    private val camera: OrthographicCamera
) : GLSurfaceView.Renderer {

    /**
     * 基础大小
     */
    private val brushResSize = 2f

    /**
     * 笔刷大小
     */
    var brushSize = 60f

    private val colorShader = ColorShader()
    private val brushShader = BrushShader(context)

    private val glColor = GLColor(floatArrayOf(0f, 0f, 0f, 0f))

    private val mixFrameBuffer = GLFrameBuffer()
    private val penFrameBuffer = GLFrameBuffer()
    private val textureShader = TextureShader(true)

    private val brushStack = ArrayList<BrushPaintRecord>()

    private val fallbackStack = ArrayList<BrushPaintRecord>()

    private val brushMap: MutableMap<String, BrushPen> = HashMap()

    private var activePen: BrushPen? = null


    private var renderAllPenRecord = false

    /**
     * 当前画笔轨迹
     */
    private var currentBrushRecord: BrushPaintRecord? = null


    fun setBrushConfig(config: BrushConfig) {
        activePen = getOrCreatePen(config)
    }

    fun clean() {
        brushStack.clear()
        currentBrushRecord = null
        mixFrameBuffer.use {
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        }
    }

    fun undo() {
        Log.d("BAAA", "撤销 ${brushStack.size}")
        val record = brushStack.removeLastOrNull() ?: return
        fallbackStack.add(record)
        currentBrushRecord = null
        renderAllPenRecord = true
    }

    fun fallback() {
        Log.d("BAAA", "恢复 fallbackStack = ${fallbackStack.size}")
        val record = fallbackStack.removeLastOrNull() ?: return
        brushStack.add(record)
        currentBrushRecord = record
        val config = BrushManager.getBrushConfigs(record.brushName) ?: return
        activePen = getOrCreatePen(config)
        val glColor = GLColor()
        glColor.setColor(record.brushColor)
        for (point in record.brushPoints) {
            brushShader.addPoint(
                BrushVertex.obtain(
                    point.x, point.y, record.brushSize, glColor.r, glColor.g, glColor.b
                )
            )
        }
//        renderAllPenRecord = true
    }

    fun onScroll(startX: Float, startY: Float, endX: Float, endY: Float, isFirstDown: Boolean) {
        val pen = activePen ?: return
        if (isFirstDown) {
            currentBrushRecord = BrushPaintRecord().apply {
                this.brushName = pen.config.name
                this.brushSize = brushSize
                this.brushColor = glColor.toColor()
                Log.d("BAAA", "入栈 startX = $startX startY = $startY")
                brushStack.add(this)
            }
            fallbackStack.clear()
        }
        hypot(startX - endX, startY - endY)

        pen.generatePoints(startX, startY, endX, endY) {
//            Log.i("BAAA", "滑动 > startX = $startX startY = $startY endX = $endX endY = $endY >>>> point = $it")
            currentBrushRecord?.brushPoints?.add(it)
            brushShader.addPoint(
                BrushVertex.obtain(
                    it.x, it.y, brushSize, glColor.r, glColor.g, glColor.b
                )
            )
        }

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        brushShader.initialize()
        textureShader.initialize()
        colorShader.initialize()
        colorShader.setColor(Color.WHITE)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        camera.updateViewport(width, height)
        mixFrameBuffer.resize(width, height)
        penFrameBuffer.resize(width, height)
        mixFrameBuffer.use {
            GL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        }
        renderAllPenRecord = true
    }

    override fun onDrawFrame(gl: GL10?) {
        camera.update()
        camera.updateModelUnitSize()
        GL.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        if (renderAllPenRecord) {
            renderAllPenRecord = false
            renderAllBrushRecord()
        } else {
            brushPenRender()
//            eraserPenRender()
        }

        GL.glEnable(GL.GL_BLEND)
        GL.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA)

        colorShader.render(camera.getMvpMatrix())
        textureShader.setTexture(mixFrameBuffer.texture)
        textureShader.render(camera.getMvpMatrix())
        GLES20.glDisable(GL.GL_BLEND)

    }

    private fun renderAllBrushRecord() {
        mixFrameBuffer.use {
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            Log.e("BAAA", "完整渲染 ${brushStack.size}")
            GL.glEnable(GL.GL_BLEND)
            GL.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA)
            GL.glBlendEquation(GL.GL_FUNC_ADD)
            val glColor = GLColor()
            brushShader.cleanPoints()
            for (record in brushStack) {
                val config = BrushManager.getBrushConfigs(record.brushName) ?: continue
                val pen = getOrCreatePen(config)
                for (point in record.brushPoints) {
                    glColor.setColor(record.brushColor)
                    brushShader.addPoint(
                        BrushVertex.obtain(
                            point.x, point.y, brushSize, glColor.r, glColor.g, glColor.b
                        )
                    )
                }
                brushShader.draw(camera.viewportRatio, camera.renderRatio, pen.getBrushTexture())

            }
            GLES20.glDisable(GL.GL_BLEND)
        }
    }

    private fun brushPenRender() {
        activePen?.let { pen ->
            mixFrameBuffer.use {
                GL.glEnable(GL.GL_BLEND)
                GL.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA)
                GL.glBlendEquation(GL.GL_FUNC_ADD)
//                GL.glBlendEquationSeparate(GL.GL_FUNC_ADD, GL.GL_FUNC_ADD)
//                GL.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA)

                brushShader.draw(camera.viewportRatio, camera.renderRatio, pen.getBrushTexture())

                GLES20.glDisable(GL.GL_BLEND)
            }
        }
    }

    private fun eraserPenRender() {
        activePen?.let { pen ->
            mixFrameBuffer.use {
                GL.glEnable(GL.GL_BLEND)
                GL.glBlendFunc(GL.GL_ZERO, GL.GL_ONE_MINUS_SRC_ALPHA)
                brushShader.draw(camera.viewportRatio, camera.renderRatio, pen.getBrushTexture())
                GLES20.glDisable(GL.GL_BLEND)
            }
        }
    }


    private fun getOrCreatePen(config: BrushConfig): BrushPen {
        if (!brushMap.containsKey(config.name)) {
            val brush = BrushPen(context, config, camera)
            brushMap[config.name] = brush
            brush.initialize()
        }
        return brushMap[config.name]!!
    }
}