package com.zipper.opengl.filter

import android.content.Context
import android.opengl.GLES20
import com.zipper.opengl.utils.MatrixHelper
import com.zipper.opengl.utils.OpenGLHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 顶点坐标 四个点
 */
val vertexPositions = floatArrayOf(
    -1.0f,
    1.0f, // 左下
    -1.0f,
    -1.0f, // 右下
    1.0f,
    1.0f, // 右上
    1.0f,
    -1.0f, // 左上
)

/**
 * 纹理坐标 四个点
 */
val textureCoords = floatArrayOf(
    0.0f,
    0.0f, // 圆点 0,0
    0.0f,
    1.0f, // 0,1
    1.0f,
    0.0f, // 1,0
    1.0f,
    1.0f, // 1,1
)

/**
 *
 * @author zhangzhipeng
 * @date 2024/4/1
 */
abstract class BaseFilter(protected val context: Context) {

    protected var programHandle = 0
    private var positionHandle = 0
    private var textureCoordinateHandle = 0
    private var matrixHandle = 0

    /**
     * 顶点坐标数据缓冲区
     */
    private val positionBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertexPositions.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
            put(vertexPositions)
            position(0)
        }

    /**
     * 纹理坐标数据缓冲区
     */
    private val textureCoordinateBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(textureCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(textureCoords)
                position(0)
            }

    open fun onSurfaceCreate() {
        // 启用2D纹理
        GLES20.glEnable(GLES20.GL_TEXTURE2)
        programHandle = OpenGLHelper.createProgram(getVertexShaderCode(), getFragmentShaderCode())
        if (programHandle <= 0) {
            return
        }
        // 获取变量句柄
        this.positionHandle = GLES20.glGetAttribLocation(this.programHandle, "vPosition")
        this.textureCoordinateHandle = GLES20.glGetAttribLocation(this.programHandle, "vCoordinate")
        this.matrixHandle = GLES20.glGetUniformLocation(this.programHandle, "vMatrix")
    }

    open fun onSurfaceChanged(matrix: FloatArray, width: Int, height: Int) {
        MatrixHelper.handleOrthoM(matrix, width, height)
    }

    @JvmOverloads
    open fun onDrawFrame(matrix: FloatArray? = null) {
        GLES20.glUseProgram(this.programHandle)
        if (matrix != null) {
            GLES20.glUniformMatrix4fv(this.matrixHandle, 1, false, matrix, 0)
        }
        GLES20.glEnable(GLES20.GL_TEXTURE2)
        GLES20.glUseProgram(this.programHandle)
        onDrawBefore(matrix)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        onDrawAfter()
    }

    open fun onDrawBefore(matrix: FloatArray?) {
        // 启用顶点数组引用
        GLES20.glEnableVertexAttribArray(this.positionHandle)
        GLES20.glEnableVertexAttribArray(this.textureCoordinateHandle)
        // 顶点赋值
        GLES20.glVertexAttribPointer(this.positionHandle, 2, GLES20.GL_FLOAT, false, 8, this.positionBuffer)
        GLES20.glVertexAttribPointer(this.textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 8, this.textureCoordinateBuffer)

        // 变换矩阵赋值
        if (matrix != null) {
            GLES20.glUniformMatrix4fv(this.matrixHandle, 1, false, matrix, 0)
        }
    }

    open fun onDrawAfter() {
        GLES20.glDisableVertexAttribArray(this.positionHandle)
        GLES20.glDisableVertexAttribArray(this.textureCoordinateHandle)
        GLES20.glUseProgram(0)
    }

    /**
     * 顶点着色器代码
     */
    protected abstract fun getVertexShaderCode(): String

    /**
     * 片元着色器代码
     */
    protected abstract fun getFragmentShaderCode(): String
}
