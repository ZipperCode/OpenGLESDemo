package com.zipper.egl_fbo

import android.opengl.GLES20
import android.opengl.GLES30

class FrameBuffer {

    private val textureHandle = IntArray(1)

    private val frameBufferHandle = IntArray(1)

    fun useFrameBuffer(width: Int, height: Int) {
        if (textureHandle[0] == 0) {
            textureHandle[0] = OpenGLHelper.createEmptyTexture(width, height)
        }

        if (frameBufferHandle[0] == 0) {
            GLES30.glGenFramebuffers(1, frameBufferHandle, 0)
        }

        if (textureHandle[0] != 0) {
            // 将纹理绑定到帧缓冲区
            // 绑定到帧缓冲区
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferHandle[0])
            // 将纹理内容附着在到帧缓冲区
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, textureHandle[0], 0)
        }
    }

    fun cancelFrameBuffer() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }
}