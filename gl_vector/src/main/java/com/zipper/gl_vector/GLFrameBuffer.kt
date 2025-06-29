package com.zipper.gl_vector

import java.nio.IntBuffer

class GLFrameBuffer {

    private val glTexture by lazy {
        GLTexture()
    }

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

    val texture get() = glTexture.texture

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        dispose()

        GL.glGetIntegerv(GL.GL_VIEWPORT, viewport)

        val fbo = IntArray(1)

        // 创建帧缓冲区
        GL.glGenFramebuffers(1, fbo, 0)
        bind()
        // 创建纹理
        glTexture.bind()
        glTexture.setFilter().setWrap().texImage2D(width, height)
        glTexture.unbind()
        // 将纹理附着在帧缓冲区中
        GL.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, glTexture.texture, 0)
        unbind()
    }

    fun bind() {
        if (glHandle == 0) {
            return
        }
        GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, glHandle)
    }

    fun unbind() {
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

    fun dispose() {
        if (glHandle != 0) {
            val fbo = intArrayOf(glHandle)
            GL.glDeleteFramebuffers(1, fbo, 0)
            glHandle = 0
        }
        glTexture.dispose()
    }
}