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

    public override fun onSurfaceCreate() {
        super.onSurfaceCreate()
        lineTextureHandle = GLES20.glGetAttribLocation(programHandle, "lineTexture")
        maskNearestTextureId = OpenGLHelper.createTexTexture(maskBitmap)
    }

    override fun onDrawFrame(matrix: FloatArray?) {
        // 关键步骤，启用颜色混合
        GLES20.glEnable(GLES20.GL_BLEND)
        // 设置混合因子 目标颜色的透明度 = 1 - 源颜色的透明度，此时，如果绘制物体的透明度小于1，则会绘制为透明色
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        super.onDrawFrame(matrix)
    }

    override fun onDrawBefore(matrix: FloatArray?) {
        super.onDrawBefore(matrix)
        GLES20.glActiveTexture(maskNearestTextureId)
        GLES20.glBindTexture(GLES20.GL_TEXTURE0, maskNearestTextureId)
        GLES20.glUniform1i(this.lineTextureHandle, 0)
    }

    override fun getVertexShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "image.vert")
    }

    override fun getFragmentShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "image.frag")
    }
}
