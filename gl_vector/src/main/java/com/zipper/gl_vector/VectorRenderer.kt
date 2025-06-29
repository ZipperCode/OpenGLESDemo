package com.zipper.gl_vector

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VectorRenderer(
    private val context: Context
) : GLSurfaceView.Renderer {

    private val orthographicCamera = OrthographicCamera()

    private val imageShader = TextureShader()
    private val colorShader = ColorShader()

    private val texture = GLTexture()

    private val identityMatrix = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f)


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        imageShader.initialize()
        val bitmap = BitmapFactory.decodeStream(context.assets.open("test.jpg"))
        texture.upload(bitmap)
        colorShader.initialize()
        val err = GL.glGetError()
        Log.d("BAAA", "err = $err")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        orthographicCamera.updateViewport(width, height)
        GL.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GL.glClear(GL.GL_COLOR_BUFFER_BIT)
        GL.glViewport(0, 0, width, height)
        val err = GL.glGetError()
        Log.d("BAAA", "err = $err")
    }

    override fun onDrawFrame(gl: GL10?) {
        val mvp = orthographicCamera.update()
//        GL.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
//        GL.glClear(GL.GL_COLOR_BUFFER_BIT)
         imageShader.draw(texture, mvp)
//        colorShader.draw(identityMatrix)
    }
}