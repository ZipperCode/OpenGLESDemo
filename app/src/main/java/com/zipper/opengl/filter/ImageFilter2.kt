package com.zipper.opengl.filter

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import com.zipper.opengl.utils.AssetsUtil
import com.zipper.opengl.utils.OpenGLHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageFilter2(context: Context) {
    private val vertexShaderCode = AssetsUtil.getAssetsContent(context, "image.vert")

    private val fragmentShaderCode = AssetsUtil.getAssetsContent(context, "image.frag")

    private val vertexCoords =
        floatArrayOf(
            -0.5f,
            0.5f,
            -0.5f,
            -0.5f,
            0.5f,
            -0.5f,
            0.5f,
            0.5f,
        )

    private val textureCoords =
        floatArrayOf(
            0f,
            0f, // top left
            0f,
            1f, // bottom left
            1f,
            1f, // bottom right
            1f,
            0f, // top right
        )

    private val vPosition = ByteBuffer.allocateDirect(vertexCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    private val vTexture = ByteBuffer.allocateDirect(textureCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    private val indices = shortArrayOf(0, 1, 2, 0, 3, 2)

    private val vIndices = ByteBuffer.allocateDirect(indices.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer()

    private var program = 0

    private var vPositionHandle = 0
    private var vCoordinateHandle = 0
    private var vTextureHandle = 0

    private val bitmap: Bitmap = AssetsUtil.getAssetsImage(context, "image.png")

    private var textureId = 0

    fun onSurfaceCreate() {
        vPosition.put(vertexCoords)
        vPosition.position(0)
        vIndices.put(indices)
        vIndices.position(0)
        vTexture.put(textureCoords)
        vTexture.position(0)
        program = OpenGLHelper.createProgram(vertexShaderCode, fragmentShaderCode)
        // 启用纹理
        GLES20.glEnable(GLES20.GL_TEXTURE2)

        vPositionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        vCoordinateHandle = GLES20.glGetAttribLocation(program, "vCoordinate")
        vTextureHandle = GLES20.glGetUniformLocation(program, "vTexture")

        // 创建纹理
        textureId = OpenGLHelper.createTexTexture(bitmap)
    }

    fun onDrawFrame() {
        vPosition.position(0)
        vIndices.position(0)
        vTexture.position(0)

        GLES20.glUseProgram(program)
        GLES20.glVertexAttribPointer(vPositionHandle, 2, GLES20.GL_FLOAT, false, 4 * 2, vPosition)
        GLES20.glVertexAttribPointer(vCoordinateHandle, 2, GLES20.GL_FLOAT, false, 4 * 2, vTexture)
        GLES20.glEnableVertexAttribArray(vPositionHandle)
        GLES20.glEnableVertexAttribArray(vCoordinateHandle)

        // 激活纹理区域
        GLES20.glActiveTexture(textureId)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
//
        GLES20.glUniform1f(vTextureHandle, 0f)

//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, vIndices)
    }
}
