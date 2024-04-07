package com.zipper.gldemo2

import android.content.Context
import android.opengl.GLES20

class LineShader(
    private val context: Context
) : BaseShader() {


    private var textureLocation = -1

    private var lineTextureId: Int = -1

    fun onSurfaceCreate() {
        programHandle[0] = OpenGLHelper.createProgram(getVertexShaderCode(), getFragmentShaderCode())

        positionLocation = getAttribLocation("aPosition")
        coordinateLocation = getAttribLocation("aTextureCoordinate")
        textureLocation = getUniformLocation("uTexture")

        val bitmap = AssetsUtil.decodeBitmapFromAssets(context, "903388/svg2png.png")
        lineTextureId = OpenGLHelper.createTexTexture(bitmap)
        bitmap?.recycle()
    }

    fun onDrawFrame() {
        GLES20.glUseProgram(programHandle[0])
        enablePositionVertex()
        enableCoordinateVertex()

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, lineTextureId)
        GLES20.glUniform1i(textureLocation, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        disablePositionVertex()
        disableCoordinateVertex()

        GLES20.glUseProgram(0)
    }

    override fun getVertexShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gles/line_shader.vert")
    }

    override fun getFragmentShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gles/line_shader.frag")
    }

}