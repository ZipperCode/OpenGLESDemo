package com.zipper.egl_fbo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import java.nio.ByteBuffer

class ImageRender(context: Context, val callback: (Bitmap) -> Unit) : ImageEGLSurface.Renderer {


    private val shader = ImageShader(context)
    private val bitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(context.resources, R.raw.org)
    }

    private val textureHandle = IntArray(1)

    private val frameBuffer = FrameBuffer()

    override fun surfaceCreated() {
        //设置背景颜色
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        textureHandle[0] = OpenGLHelper.createTexture(bitmap)
        shader.onSurfaceCreate()
        GLES30.glViewport(0, 0, bitmap.width, bitmap.height)
    }

    override fun onDrawFrame() {
        GLES30.glClearColor(0.5f, 0.7f, 0.3f, 1.0f)
        // 将颜色缓存区设置为预设的颜色
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        frameBuffer.useFrameBuffer(bitmap.width, bitmap.height)
        shader.draw(textureHandle[0])
        createOutBitmap()
        frameBuffer.cancelFrameBuffer()
    }

    private fun createOutBitmap() {
        val byteBuffer = ByteBuffer.allocate(bitmap.width * bitmap.height * 4)
        GLES30.glReadPixels(0, 0, bitmap.width, bitmap.height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, byteBuffer)
        val outBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(byteBuffer)
        callback(bitmap)
    }
}