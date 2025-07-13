package com.zipper.gldemo.pen

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLSurfaceView
import com.zipper.gl.base.GL
import com.zipper.gl.base.GLFrameBuffer
import com.zipper.gl.base.OrthographicCamera
import com.zipper.gldemo.pen.shader.BrushShader
import com.zipper.gldemo.pen.shader.ColorShader
import com.zipper.gldemo.pen.shader.TextureShader
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

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
    private var brushSize = 20f

    private val viewWidth get() = camera.viewportWidth
    private val viewHeight get() = camera.viewportHeight

    private val colorShader = ColorShader()
    private val brushShader = BrushShader(context)

    private val colorArray = floatArrayOf(1f, 0f, 0f, 1f)

    private val mixFrameBuffer = GLFrameBuffer()
    private val penFrameBuffer = GLFrameBuffer()
    private val textureShader = TextureShader(true)

    fun onScroll(startX: Float, startY: Float, endX: Float, endY: Float) {
        val minR = camera.getMinRange()
        val maxR = camera.getMaxRange()
        BrushPointHelper.brushPointCreator(startX, startY, endX, endY, viewWidth, viewHeight, minR, maxR, 4) {
            brushShader.addPoint(
                BrushPointInfo(
                    it.x, it.y, colorArray[0], colorArray[1], colorArray[2], colorArray[3],
                    BrushPointHelper.convertBrushRenderSize(brushResSize, brushSize, camera.scale)
                )
            )
        }
        brushShader.syncVertexBuffer()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        brushShader.initialize()
        brushShader.brushTexture.upload(BitmapFactory.decodeStream(context.assets.open("brush.png")))
        textureShader.initialize()
        colorShader.initialize()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mixFrameBuffer.resize(width, height)
        penFrameBuffer.resize(width, height)
//        mixFrameBuffer.use {
//            GL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
//        }
//        penFrameBuffer.use {
//            GL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
//        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GL.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        mixFrameBuffer.use {
            GL.glEnable(GL.GL_BLEND)
//            GL.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA)
//            GL.glBlendEquation(GL.GL_FUNC_ADD)
            GL.glBlendEquationSeparate(GL.GL_FUNC_ADD, GL.GL_FUNC_ADD)
            GL.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA)
            brushShader.render(camera.viewportRatio, camera.renderRatio)
            GLES20.glDisable(GL.GL_BLEND)
        }

        GL.glEnable(GL.GL_BLEND)
        GL.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA)

        textureShader.setTexture(mixFrameBuffer.texture)
        textureShader.render(camera.getMvpMatrix())
        GLES20.glDisable(GL.GL_BLEND)

    }
}