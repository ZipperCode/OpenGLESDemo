package com.zipper.gl_vector

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log

class DrawImageRender(
    private val context: Context
): RenderAdapter {

    private val orthographicCamera = OrthographicCamera()

    private val imageShader = TextureShader()
    private val colorShader = ColorShader()

    private val texture = GLTexture()
    private val identityMatrix = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f)


    override fun onCreate() {
        imageShader.initialize()
        val bitmap = BitmapFactory.decodeStream(context.assets.open("test.jpg"))
        texture.upload(bitmap)
        colorShader.initialize()
        val err = GL.glGetError()
        Log.d("BAAA", "err1 = $err")
        Log.d("BAAA", "onCreate")

    }

    override fun onSize(width: Int, height: Int) {
        orthographicCamera.updateViewport(width, height)
        GL.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GL.glClear(GL.GL_COLOR_BUFFER_BIT)
        GL.glViewport(0, 0, width, height)
        Log.d("BAAA", "onSize")
    }

    override fun onRender() {
        GL.glClearColor(1.0f, 0.0f, 1.0f, 1.0f)
        GL.glClear(GL.GL_COLOR_BUFFER_BIT)
        // Log.d("BAAA", "Render")
        val mvp = orthographicCamera.update()
//        GL.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
//        GL.glClear(GL.GL_COLOR_BUFFER_BIT)
        imageShader.draw(texture, mvp)
        val err = GL.glGetError()
        Log.d("BAAA", "err2 = $err")
//        colorShader.draw(identityMatrix)
    }

    override fun onDispose() {

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