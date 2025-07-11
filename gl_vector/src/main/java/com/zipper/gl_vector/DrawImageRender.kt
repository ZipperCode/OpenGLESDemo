package com.zipper.gl_vector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.zipper.gl_vector.gl.GLFrameBuffer
import com.zipper.gl_vector.gl.GLTexture
import com.zipper.gl_vector.gl.OrthographicCamera
import com.zipper.gl_vector.shader.TextureShader
import com.zipper.gl_vector.shader.VectorShader
import com.zipper.gl_vector.surfaceview.RenderAdapter

class DrawImageRender(
    private val context: Context
) : RenderAdapter {

    private val orthographicCamera = OrthographicCamera()

    private val imageShader = TextureShader()
    private val colorShader = ColorShader()
    private val lineShader = VectorShader()

    private val texture = GLTexture()
    private val maskTexture = GLTexture()


    override fun onCreated() {
        imageShader.initialize()
        colorShader.initialize()
        lineShader.initialize()
    }

    fun uploadLine(bitmap: Bitmap) {
        texture.upload(bitmap)
        orthographicCamera.updateRenderSize(bitmap.width, bitmap.height)
    }

    fun uploadMask(bitmap: Bitmap) {
        maskTexture.upload(bitmap)
        orthographicCamera.updateRenderSize(bitmap.width, bitmap.height)
    }


    override fun onSizeChanged(width: Int, height: Int) {
        orthographicCamera.updateViewport(width, height)
        GL.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GL.glClear(GL.GL_COLOR_BUFFER_BIT)
        GL.glViewport(0, 0, width, height)
        Log.d("BAAA", "onSize")
    }

    override fun onRender() {
        GL.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GL.glClear(GL.GL_COLOR_BUFFER_BIT)
        // Log.d("BAAA", "Render")
        val mvp = orthographicCamera.update()
        GL.glEnable(GL.GL_BLEND)
        GL.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA)
        imageShader.setTexture(maskTexture)
        imageShader.render(mvp)
        lineShader.glTexture = texture
        lineShader.render(mvp)
        GL.glDisable(GL.GL_BLEND)
        val err = GL.glGetError()
        //Log.d("BAAA", "err2 = $err")
//        colorShader.draw(identityMatrix)
    }

    override fun onDispose() {

    }

    override fun onDown(x: Float, y: Float) {
        super.onDown(x, y)


    }

    override fun onScale(scale: Float, focusX: Float, focusY: Float) {
        super.onScale(scale, focusX, focusY)
        orthographicCamera.onScale(scale, focusX, focusY)
    }

    override fun onTranslate(dx: Float, dy: Float) {
        Log.i("BAAA", "onTranslate dx = $dx dy = $dy")
        orthographicCamera.onTranslate(dx, dy)
    }
}