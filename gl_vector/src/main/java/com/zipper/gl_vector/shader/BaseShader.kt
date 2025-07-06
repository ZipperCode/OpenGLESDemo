package com.zipper.gl_vector.shader

import com.zipper.gl_vector.DrawConstant
import com.zipper.gl_vector.GL
import com.zipper.gl_vector.gl.ShaderProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 *
 * @author  zhangzhipeng
 * @date    2025/7/4
 */
abstract class BaseShader {
    private var vertexBuffer = ByteBuffer.allocateDirect(DrawConstant.VERTEX.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(DrawConstant.VERTEX)
        .position(0)
    private var textureBuffer = ByteBuffer.allocateDirect(DrawConstant.TEXTURE.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(DrawConstant.TEXTURE)
        .position(0)

    protected val program by lazy {
        ShaderProgram(getVertexShaderCode(), getFragmentShaderCode())
    }

    open fun initialize() {
        // program.dispose()
        program.initialize()
    }

    open fun render(mvpMatrix: FloatArray) {
        program.useProgram()
        vertexBuffer.position(0)
        textureBuffer.position(0)
        if (program.checkUniformLocation("uMatrix")) {
            program.setMatrix("uMatrix", mvpMatrix)
        }
        if (program.checkAttributeLocation("aPosition")) {
            program.setVertexAttribute("aPosition", 2, GL.GL_FLOAT, false, 0, vertexBuffer)
        }
        if (program.checkAttributeLocation("aTextureCoordinate")) {
            program.setVertexAttribute("aTextureCoordinate", 2, GL.GL_FLOAT, false, 0, textureBuffer)
        }
    }

    fun setVertexBuffer(vertex: FloatArray) {
        vertexBuffer = ByteBuffer.allocateDirect(vertex.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertex)
            .position(0)
    }

    fun setVertexBuffer(vertex: FloatBuffer) {
        vertexBuffer = vertex
    }

    fun setTextureCoordinateBuffer(texture: FloatArray) {
        textureBuffer = ByteBuffer.allocateDirect(texture.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(texture)
            .position(0)
    }
    fun setTextureCoordinateBuffer(texture: FloatBuffer) {
        textureBuffer = texture
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