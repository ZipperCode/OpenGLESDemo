package com.zipper.opengl.filter

import android.opengl.GLES20
import com.zipper.opengl.utils.OpenGLHelper

/**
 *
 * @author zhangzhipeng
 * @date 2024/4/1
 */
class OffscreenBufferHelper {

    private var width = 0
    private var height = 0

    private var frameBufferHandle = 0

    private var textureId = 0

    var isInit = false
        private set

    /**
     * 绑定空白纹理到自定义缓冲区
     */
    fun switchCustomBuffer() {
        if (this.textureId == 0) {
            this.textureId = OpenGLHelper.createTexture(this.width, this.height)
        }
        if (this.textureId > 0) {
            // 将纹理绑定到自定义的缓冲区
            GLES20.glBindTexture(GLES20.GL_FRAMEBUFFER, this.textureId)
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, this.textureId, 0)
        }
    }

    /**
     * 切换到系统缓冲区
     */
    fun switchSystemBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    fun getTextureId(): Int {
        return this.textureId
    }

    /**
     * 创建帧缓冲区
     */
    fun createFrameBuffer(width: Int, height: Int): Boolean {
        this.width = width
        this.height = height
        val arr = IntArray(1)
        GLES20.glGenFramebuffers(1, arr, 0)
        this.frameBufferHandle = arr[0]
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.frameBufferHandle)
        isInit = true
        return this.frameBufferHandle > 0
    }
}
