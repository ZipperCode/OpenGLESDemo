package com.zipper.opengl.filter

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import com.zipper.opengl.utils.AssetsUtil
import com.zipper.opengl.utils.MatrixHelper
import com.zipper.opengl.utils.OpenGLHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 *
 * @author zhangzhipeng
 * @date 2024/4/1
 */
class LineFilter2(context: Context) {

    private val vertexPosition: FloatArray = floatArrayOf(
        -1.0f,
        1.0f, // 左下
        -1.0f,
        -1.0f, // 右下
        1.0f,
        1.0f, // 右上
        1.0f,
        -1.0f, // 左上
    )

    private val textureCoordinate: FloatArray = floatArrayOf(
        0.0f,
        0.0f, // 圆点 0,0
        0.0f,
        1.0f, // 0,1
        1.0f,
        0.0f, // 1,0
        1.0f,
        1.0f, // 1,1
    )

//    private val vertexShaderCode = AssetsUtil.getAssetsContent(context, "glsl/com_base_gl_fbo_v.glsl")
//
//    private val fragmentShaderCode = AssetsUtil.getAssetsContent(context, "glsl/com_gl_line_2.glsl")

    private val vertexShaderCode = AssetsUtil.getAssetsContent(context, "image.vert")

    private val fragmentShaderCode = AssetsUtil.getAssetsContent(context, "image.frag")

    private var programHandle = 0

    private var positionHandle = 0

    private var matrixHandle = 0

    private var textureCoordinateHandle = 0

    private val indices = shortArrayOf(0, 1, 2, 0, 2, 3)

    private var vPositionBuffer = ByteBuffer.allocateDirect(vertexPosition.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    private val vTextureBuffer = ByteBuffer.allocateDirect(textureCoordinate.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    private val vIndexBuffer = ByteBuffer.allocateDirect(indices.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer()

    private val maskBitmap: Bitmap = AssetsUtil.getAssetsImage(context, "902684/maskCombine.png")

    private var maskNearestTextureId = 0

    private var lineTextureHandle = 0


    fun onSurfaceCreate() {
        programHandle = OpenGLHelper.createProgram(vertexShaderCode, fragmentShaderCode)
        vPositionBuffer.put(vertexPosition)
        vPositionBuffer.position(0)

        vTextureBuffer.put(textureCoordinate)
        vTextureBuffer.position(0)

        vIndexBuffer.put(indices)
        vIndexBuffer.position(0)
        GLES20.glEnable(GLES20.GL_TEXTURE2)

        positionHandle = GLES20.glGetAttribLocation(programHandle, "vPosition")
        textureCoordinateHandle = GLES20.glGetAttribLocation(programHandle, "vCoordinate")
        matrixHandle = GLES20.glGetUniformLocation(programHandle, "vMatrix")

        lineTextureHandle = GLES20.glGetAttribLocation(programHandle, "lineTexture")

        maskNearestTextureId = OpenGLHelper.createTexTexture(maskBitmap)
    }

    private val matrix: FloatArray = FloatArray(16)

    fun onSurfaceChanged(width: Int, height: Int) {
        MatrixHelper.handleOrthoM(matrix, width, height)
    }

    fun onDrawFrame() {
        // 关键步骤，启用颜色混合
        GLES20.glEnable(GLES20.GL_BLEND)
        // 设置混合因子 目标颜色的透明度 = 1 - 源颜色的透明度，此时，如果绘制物体的透明度小于1，则会绘制为透明色
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        vPositionBuffer.position(0)
        vTextureBuffer.position(0)
        vIndexBuffer.position(0)

        GLES20.glUseProgram(programHandle)

        GLES20.glEnableVertexAttribArray(this.positionHandle)
        GLES20.glVertexAttribPointer(this.positionHandle, 2, GLES20.GL_FLOAT, false, 8, this.vPositionBuffer)
        GLES20.glEnableVertexAttribArray(this.textureCoordinateHandle)
        GLES20.glVertexAttribPointer(this.textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 8, this.vTextureBuffer)

        GLES20.glUniformMatrix4fv(this.matrixHandle, 1, false, this.matrix, 0)

        GLES20.glActiveTexture(maskNearestTextureId)
        GLES20.glBindTexture(GLES20.GL_TEXTURE0, maskNearestTextureId)
        GLES20.glUniform1i(this.lineTextureHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, vIndexBuffer)
    }
}
