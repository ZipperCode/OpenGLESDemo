package com.opensource.gradientdemo.buffer

import android.opengl.GLES20.GL_COLOR_ATTACHMENT0
import android.opengl.GLES20.GL_FRAMEBUFFER
import android.opengl.GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT
import android.opengl.GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS
import android.opengl.GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT
import android.opengl.GLES20.GL_FRAMEBUFFER_UNSUPPORTED
import android.opengl.GLES20.GL_NONE
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.glBindFramebuffer
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glCheckFramebufferStatus
import android.opengl.GLES20.glDeleteFramebuffers
import android.opengl.GLES20.glDeleteTextures
import android.opengl.GLES20.glFramebufferTexture2D
import android.opengl.GLES20.glGenFramebuffers
import android.util.Log
import com.opensource.gradientdemo.utils.OpenGLHelper

/**
 *
 * @author  zhangzhipeng
 * @date    2025/1/8
 */
class FrameBuffer {
    private val fboId = IntArray(1)
    private val textureId = IntArray(1)
    private var fboWidth: Int = 0
    private var fboHeight: Int = 0
    private var isInitialized = false

    fun init(width: Int, height: Int) {
        if (isInitialized && fboWidth == width && fboHeight == height) {
            // 初始化过了
            return
        }
        release()
        this.fboWidth = width
        this.fboHeight = height
        // 创建新的纹理
        textureId[0] = OpenGLHelper.createFBOTexture(width, height)
        glGenFramebuffers(1, fboId, 0)
        // 绑定到帧缓冲区
        glBindFramebuffer(GL_FRAMEBUFFER, fboId[0])
        // 将纹理内容附着在到帧缓冲区
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId[0], 0)
        // 检查状态
        val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        printBufferError(status)
        // 解绑
        glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE)
        isInitialized = true
    }

    fun with(block: () -> Unit) {
        glBindFramebuffer(GL_FRAMEBUFFER, fboId[0])
        block()
        glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE)
    }

    fun release() {
        if (textureId[0] > 0) {
            // 绑定到默认纹理，好删除旧纹理
            glBindTexture(GL_TEXTURE_2D, 0)
            glDeleteTextures(1, textureId, 0)
            textureId[0] = 0
        }
        if (fboId[0] > 0) {
            // 解绑缓冲区后删除缓冲区
            glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE)
            glDeleteFramebuffers(1, fboId, 0)
            fboId[0] = 0
        }
        isInitialized = false
        fboWidth = 0
        fboHeight = 0
    }

    fun getTextureId(): Int = textureId[0]

    private fun printBufferError(status: Int) {
        when (status) {
            GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> {
                Log.e("BAAA", "缓冲区状态异常 附件未完成")
            }

            GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> {
                Log.e("BAAA", "缓冲区状态异常 附件丢失")
            }

            GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS -> {
                Log.e("BAAA", "缓冲区状态异常 维度不匹配")
            }

            GL_FRAMEBUFFER_UNSUPPORTED -> {
                Log.e("BAAA", "缓冲区状态异常 不支持")
            }

            else -> {}
        }
    }
}