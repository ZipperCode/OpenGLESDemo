package com.zipper.egl_fbo

import android.opengl.GLES10
import android.opengl.GLES30
import javax.microedition.khronos.opengles.GL10

typealias GL = GLES30

class FrameBuffer3 {
    /**
     * 多重采样FBO
     */
    private val msaaFbo = IntArray(1)
    private val msaaRbo = IntArray(1)

    /**
     * 多重采样后的fbo不能直接输出到屏幕，需要解析到普通纹理
     */
    private val resolveFbo = IntArray(1)
    private val resolveTex = IntArray(1)

    private var width = 0
    private var height = 0

    fun getTextureId() = resolveTex[0]

    fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        GL.glEnable(GLES10.GL_MULTISAMPLE)
        release()
        // 创建 MASS FBO
        GL.glGenFramebuffers(1, msaaFbo, 0)
        GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, msaaFbo[0])
        // 创建多采样颜色缓冲区
        GL.glGenRenderbuffers(1, msaaRbo, 0)
        GL.glBindRenderbuffer(GL.GL_RENDERBUFFER, msaaRbo[0])
        GLES30.glRenderbufferStorageMultisample(GL.GL_RENDERBUFFER, 4, GLES30.GL_RGBA8, width, height)
        GL.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_RENDERBUFFER, msaaRbo[0])

        // 创建解析FBO
        GL.glGenFramebuffers(1, resolveFbo, 0)
        GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, resolveFbo[0])
        // 创建解析纹理
        GL.glGenTextures(1, resolveTex, 0)
        GL.glBindTexture(GL.GL_TEXTURE_2D, resolveTex[0])
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR)
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR)
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE)
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE)
        GL.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null)
        GL.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D,
            resolveTex[0],
            0
        )
        GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0)
    }

    fun withBuffer(block: () -> Unit) {
        GL.glEnable(GL10.GL_MULTISAMPLE)
        // 绑定到 MASS FBO
        GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, msaaFbo[0])
        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.glClear(GL.GL_COLOR_BUFFER_BIT)
        GL.glViewport(0, 0, width, height)
        block()
        // 将MSAA内容解析到普通纹理
        GL.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, msaaFbo[0])
        GL.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, resolveFbo[0])
        GLES30.glBlitFramebuffer(
            0, 0, width, height,
            0, 0, width, height,
            GL.GL_COLOR_BUFFER_BIT, GL.GL_NEAREST
        )

        GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0)
    }


    fun release() {
        if (msaaFbo[0] > 0) {
            GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0)
            GL.glDeleteFramebuffers(1, msaaFbo, 0)
        }
        if (resolveTex[0] > 0) {
            GL.glBindTexture(GL.GL_TEXTURE_2D, 0)
            GL.glDeleteTextures(1, resolveTex, 0)
        }
        if (msaaRbo[0] > 0) {
            GL.glBindRenderbuffer(GL.GL_RENDERBUFFER, 0)
            GL.glDeleteRenderbuffers(1, msaaRbo, 0)
        }
    }
}