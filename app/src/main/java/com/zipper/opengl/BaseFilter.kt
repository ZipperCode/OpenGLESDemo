package com.zipper.opengl

import android.opengl.GLES20
import com.zipper.opengl.utils.OpenGLHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

abstract class BaseFilter(
    protected val vertexCoords: FloatArray,
) {
    protected var program = 0

    protected val vPosition: FloatBuffer =
        ByteBuffer
            .allocateDirect(vertexCoords.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()

    protected var vPositionHandle = 0

    fun createProgram(
        vertexSource: String,
        fragmentSource: String,
    ) {
        program = OpenGLHelper.createProgram(vertexSource, fragmentSource)
    }

    fun baseOnDraw() {
        // 清除屏幕
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        // 重置顶点位置
        vPosition.position(0)
        if (program == 0) {
            return
        }
        GLES20.glUseProgram(program)

        vPositionHandle = GLES20.glGetAttribLocation(program, "vPosition")

        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 4 * 3, vPosition)
    }
}
