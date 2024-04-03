package com.zipper.opengl.filter

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import com.zipper.opengl.utils.AssetsUtil
import com.zipper.opengl.utils.OpenGLHelper

/**
 *
 * @author zhangzhipeng
 * @date 2024/4/1
 */
class LineFilter(context: Context) : BaseFilter(context) {

    private val maskBitmap: Bitmap = AssetsUtil.getAssetsImage(context, "902684/maskCombine.png")

    private var maskNearestTextureId = 0

    private var lineTextureHandle = 0

    override fun onSurfaceCreate() {
        super.onSurfaceCreate()
        lineTextureHandle = GLES20.glGetAttribLocation(programHandle, "lineTexture")
        maskNearestTextureId = OpenGLHelper.createTexTexture(maskBitmap)
    }

    fun draw(matrix: FloatArray) {
        useProgram(matrix)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, maskNearestTextureId)
        GLES20.glUniform1i(this.lineTextureHandle, 0)
        enablePointer()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        disablePointer()
        GLES20.glUseProgram(0)
    }

    override fun getVertexShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "image.vert")
    }

    override fun getFragmentShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "image.frag")
    }
}
