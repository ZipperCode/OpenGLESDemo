package com.zipper.gldemo2.shader

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import com.sinyee.babybus.kidscolor.egl.FrameBuffer
import com.zipper.gldemo2.AssetsUtil

class FrameBufferTextureShader(
    private val context: Context
) : BaseShader() {

    private val frameBuffer = FrameBuffer()

    private var textureLocation = -1

    fun initFrameBuffer(width: Int, height: Int) {
        frameBuffer.init(width, height)
    }

    fun withFrameBuffer(block: () -> Unit) {
        frameBuffer.bindFrameBuffer()
        block()
        frameBuffer.unbindFrameBuffer()
    }

    fun getTextureId(): Int {
        return frameBuffer.getTextureId()
    }

    fun onSurfaceCreate() {
        initProgram()
        textureLocation = getUniformLocation("uTexture")
    }

    fun onDrawFrame(matrix: FloatArray) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        val textureId = frameBuffer.getTextureId()
        GLES20.glUseProgram(programHandle[0])
        enablePositionVertex()
        enableCoordinateVertex()
//        Log.d("BAAA", "err = ${GLES20.glGetError()}")
        GLES20.glUniformMatrix4fv(matrixLocation, 1, false, matrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureLocation, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        disablePositionVertex()
        disableCoordinateVertex()

        GLES20.glUseProgram(0)
    }

    override fun getVertexShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gles/fbo_tex_shader.vert")

    }

    override fun getFragmentShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gles/fbo_tex_shader.frag")
    }
}