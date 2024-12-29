package com.zipper.gldemo2.paint

import android.opengl.GLES20.*
import java.nio.IntBuffer

class Framebuffer {

    private var frameBufferId: Int = 0
    private var renderBufferId: Int = 0
    private var textureId: Int = 0
    private var isInitialized: Boolean = false
    var width: Int = 0
        private set
    var height: Int = 0
        private set

    fun init(width: Int, height: Int) {
        if (isInitialized && this.width == width && this.height == height) {
            return // 如果已经初始化且宽高相同，则直接返回
        }

        release() // 释放之前的资源

        this.width = width
        this.height = height

        // 1. 创建 Framebuffer 对象
        val fb = IntBuffer.allocate(1)
        glGenFramebuffers(1, fb)
        frameBufferId = fb[0]
        if (frameBufferId == 0) {
            throw RuntimeException("Could not create framebuffer")
        }

        // 2. 创建纹理附件
        val textures = IntBuffer.allocate(1)
        glGenTextures(1, textures)
        textureId = textures[0]
        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glBindTexture(GL_TEXTURE_2D, 0)

        // 3. 创建 Renderbuffer 对象（用于深度附件，可选）
        val rb = IntBuffer.allocate(1)
        glGenRenderbuffers(1, rb)
        renderBufferId = rb[0]
        glBindRenderbuffer(GL_RENDERBUFFER, renderBufferId)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height)
        glBindRenderbuffer(GL_RENDERBUFFER, 0)

        // 4. 将纹理和 Renderbuffer 附加到 Framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBufferId)

        // 5. 检查 Framebuffer 的完整性
        val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            val errorString = when (status) {
                GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT"
                GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT"
                GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS -> "GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS"
                GL_FRAMEBUFFER_UNSUPPORTED -> "GL_FRAMEBUFFER_UNSUPPORTED"
                else -> "Unknown Framebuffer error: $status"
            }
            throw RuntimeException("Framebuffer is not complete: $errorString")
        }
        glClearColor(0f, 0f, 0f, 0f)
        glBindFramebuffer(GL_FRAMEBUFFER, 0) // 解绑 Framebuffer

        isInitialized = true
    }

    fun withFrame(block: () -> Unit) {
        if (!isInitialized) {
            throw IllegalStateException("Framebuffer is not initialized. Call init() first.")
        }
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId)
//        glViewport(0, 0, width, height)
        block()
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun getTextureId(): Int {
        if (!isInitialized) {
            throw IllegalStateException("Framebuffer is not initialized. Call init() first.")
        }
        return textureId
    }

    fun release() {
        if (!isInitialized) return

        val fb = IntBuffer.allocate(1).put(frameBufferId)
        fb.position(0)
        glDeleteFramebuffers(1, fb)

        val rb = IntBuffer.allocate(1).put(renderBufferId)
        rb.position(0)
        glDeleteRenderbuffers(1, rb)

        val textures = IntBuffer.allocate(1).put(textureId)
        textures.position(0)
        glDeleteTextures(1, textures)

        frameBufferId = 0
        renderBufferId = 0
        textureId = 0
        isInitialized = false
        width = 0
        height = 0
    }
}