package com.sinyee.babybus.kidscolor.egl

import android.opengl.GLES20
import android.util.Log
import com.zipper.gldemo2.OpenGLHelper

class FrameBuffer {

    private val textureId = IntArray(1)

    private val frameBufferHandle = IntArray(1)


    fun initFrameBuffer(width: Int, height: Int) {
        if (textureId[0] > 0) {
            // 绑定到默认纹理，好删除旧纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            GLES20.glDeleteTextures(1, textureId, 0)
        }
        textureId[0] = OpenGLHelper.createFBOTexture(width, height)

        if (frameBufferHandle[0] > 0) {
            // 解绑缓冲区后删除缓冲区
            unbindFrameBuffer()
            GLES20.glDeleteFramebuffers(1, frameBufferHandle, 0)
        }
        GLES20.glGenFramebuffers(1, frameBufferHandle, 0)
    }

    fun bindFrameBuffer() {
        // 绑定到帧缓冲区
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferHandle[0])
        // 将纹理内容附着在到帧缓冲区
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId[0], 0)

        val glCheckFramebufferStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)

        if (glCheckFramebufferStatus != GLES20.GL_COMPILE_STATUS) {
            Log.e("BAAA", "缓冲区状态异常 status = $glCheckFramebufferStatus")
        }
    }

    fun unbindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    fun getTextureHandle(): Int {
        return textureId[0]
    }
}