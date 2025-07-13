package com.zipper.gl.base

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.IntBuffer

class GLFrameBuffer {

    private val glTexture = GLTexture()

    private var glHandle: Int = 0

    var width = 1
        private set
    var height = 1
        private set

    private val viewport = IntBuffer.allocate(4)

    private val viewPortLeft get() = viewport.array()[0]
    private val viewPortTop get() = viewport.array()[1]
    private val viewPortRight get() = viewport.array()[2]
    private val viewPortBottom get() = viewport.array()[3]

    val texture get() = glTexture

    private fun init() {
        GL.glGetIntegerv(GL.GL_VIEWPORT, viewport)
        glTexture.init()
        val fbo = IntArray(1)
        // 创建帧缓冲区
        GL.glGenFramebuffers(1, fbo, 0)
        glHandle = fbo[0]
    }

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        dispose()
        init()
        bind()
        // 创建纹理
        glTexture.bind()
        glTexture.setFilter().setWrap().texImage2D(width, height)
        glTexture.unbind()
        // 将纹理附着在帧缓冲区中
        GL.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, glTexture.texture, 0)
        unbind()
    }

    private fun bind() {
        if (glHandle == 0) {
            return
        }
        GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, glHandle)
    }

    private fun unbind() {
        GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0)
    }

    fun use(block: () -> Unit) {
        bind()
        GL.glViewport(0, 0, width, height)
        block()
        unbind()
        // 缓冲区大小可能和屏幕不一样，所以需要重置
        if (viewPortLeft != 0 || viewPortTop != 0 || viewPortRight != width || viewPortBottom != height) {
            GL.glViewport(viewPortLeft, viewPortTop, viewPortRight, viewPortBottom)
        }
    }

    fun outputBitmap(): Bitmap? {
        val originalFramebuffer = IntArray(1)
        // 获取当前绑定的帧缓冲
        GL.glGetIntegerv(GL.GL_FRAMEBUFFER_BINDING, originalFramebuffer, 0)
        // 创建新的缓冲区，使用RenderBuffer作为纹理绘制
        val framebufferArray = IntArray(1)
        GL.glGenFramebuffers(1, framebufferArray, 0)
        GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, framebufferArray[0])
        val renderBufferArray = IntArray(1)
        GL.glGenRenderbuffers(1, renderBufferArray, 0)
        GL.glBindRenderbuffer(GL.GL_RENDERBUFFER, renderBufferArray[0])
        GL.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL.GL_DEPTH_COMPONENT16, width, height)
        GL.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, glTexture.texture, 0)
        GL.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, GL.GL_RENDERBUFFER, renderBufferArray[0])
        GL.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER)
        val pixelBuffer = ByteBuffer.allocateDirect(width * height * 4)
        pixelBuffer.position(0)
        GL.glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, pixelBuffer)
        pixelBuffer.rewind()
        try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(pixelBuffer)
            return bitmap
        } catch (e: Error) {
            e.printStackTrace()
        } finally {
            GL.glDeleteRenderbuffers(1, renderBufferArray, 0)
            GL.glDeleteFramebuffers(1, framebufferArray, 0)
            GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, originalFramebuffer[0])
        }

        return null
    }

    private fun dispose() {
        if (glHandle != 0) {
            val fbo = intArrayOf(glHandle)
            GL.glDeleteFramebuffers(1, fbo, 0)
            glHandle = 0
        }
        glTexture.dispose()
    }
}