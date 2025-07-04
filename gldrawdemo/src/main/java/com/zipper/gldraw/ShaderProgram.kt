package com.zipper.gldraw

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer


/**
 *
 * @author  zhangzhipeng
 * @date    2025/6/23
 */
open class ShaderProgram constructor(
    private val vertexShader: String,
    private val fragmentShader: String
) {

    companion object {
        /**
         * 顶点数据
         */
        const val POSITION_ATTRIBUTE = "aPosition"

        /**
         * 纹理坐标
         */
        const val TEX_COORDINATE_ATTRIBUTE = "aTextureCoordinate"

        /**
         * 变化矩阵
         */
        const val MATRIX_ATTRIBUTE = "uMatrix"
    }

    private val uniforms = HashMap<String, Int>()
    private val attributes = HashMap<String, Int>()

    private var programHandle = 0
    private var vertexShaderHandle = 0
    private var fragmentShaderHandle = 0

    /**
     * 是否需要重新编译
     */
    private var invalidated = false

    var isCompiled = false
        private set

    private var errorLog = ""

    init {
        compileShader()
        if (isCompiled) {
            fetchAttributes()
            fetchUniforms()
        }
    }

    private fun compileShader() {
        vertexShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        fragmentShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        if (vertexShaderHandle == -1 || fragmentShaderHandle == -1) {
            isCompiled = false
            return
        }
        val program = linkProgram(GLES20.glCreateProgram())
        if (program == -1) {
            isCompiled = false
            return
        }
        programHandle = program
        isCompiled = true
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) {
            return -1
        }
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        val intBuf = IntBuffer.allocate(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, intBuf)
        if (intBuf.get(0) == 0) {
            val info = GLES20.glGetShaderInfoLog(shader)
            errorLog += if (type == GLES20.GL_VERTEX_SHADER) "Vertex shader\n" else "Fragment shader:\n"
            errorLog += info
            return -1
        }
        return shader
    }

    private fun linkProgram(program: Int): Int {
        if (program == 0) {
            return -1
        }
        GLES20.glAttachShader(program, vertexShaderHandle)
        GLES20.glAttachShader(program, fragmentShaderHandle)
        GLES20.glLinkProgram(program)
        val tmp = ByteBuffer.allocateDirect(4)
        tmp.order(ByteOrder.nativeOrder())
        val intBuf = tmp.asIntBuffer()
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, intBuf)
        if (intBuf.get(0) == 0) {
            errorLog += "Error linking program:\n"
            errorLog += GLES20.glGetProgramInfoLog(program)
            return -1
        }
        return program
    }

    fun fetchAttributeLocation(name: String): Int {
        var location = uniforms.getOrDefault(name, -1)
        if (location == -1) {
            location = GLES20.glGetUniformLocation(programHandle, name)
            uniforms[name] = location
        }
        return location
    }

    fun fetchUniformLocation(name: String): Int {
        var location = uniforms.getOrDefault(name, -1)
        if (location == -1) {
            location = GLES20.glGetUniformLocation(programHandle, name)
            uniforms[name] = location
        }
        return location
    }

    fun setUniform1i(name: String, value: Int) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GLES20.glUniform1i(location, value)
    }

    fun setUniform2i(name: String, value1: Int, value2: Int) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GLES20.glUniform2i(location, value1, value2)
    }

    fun setUniform3i(name: String, value1: Int, value2: Int, value3: Int) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GLES20.glUniform3i(location, value1, value2, value3)
    }

    fun setUniform4i(name: String, value1: Int, value2: Int, value3: Int, value4: Int) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GLES20.glUniform4i(location, value1, value2, value3, value4)
    }

    fun setUniform1f(name: String, value: Float) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GLES20.glUniform1f(location, value)
    }

    fun setUniform2f(name: String, value1: Float, value2: Float) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GLES20.glUniform2f(location, value1, value2)
    }

    fun setUniform3f(name: String, value1: Float, value2: Float, value3: Float) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GLES20.glUniform3f(location, value1, value2, value3)
    }

    fun setUniform4f(name: String, value1: Float, value2: Float, value3: Float, value4: Float) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GLES20.glUniform4f(location, value1, value2, value3, value4)
    }

    fun setUniform1fv(name: String, count: Int, values: FloatArray, offset: Int) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GLES20.glUniform1fv(location, count, values, offset)
    }

    fun setUniform2fv(name: String, count: Int, values: FloatArray, offset: Int) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GLES20.glUniform2fv(location, count / 2, values, offset)
    }


    fun setUniformMatrix(name: String, matrix: FloatArray, transpose: Boolean = false) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GLES20.glUniformMatrix4fv(location, 1, transpose, matrix, 0)
    }

    fun setVertexAttribute(name:String, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int) {
        checkManaged()
        val location = fetchAttributeLocation(name)
        GLES20.glVertexAttribPointer(location, size, type, normalized, stride, offset)
    }

    fun enableVertexAttribute(name: String) {
        checkManaged()
        val location = fetchAttributeLocation(name)
        GLES20.glEnableVertexAttribArray(location)
    }

    fun disableVertexAttribute(name: String) {
        checkManaged()
        val location = fetchAttributeLocation(name)
        GLES20.glDisableVertexAttribArray(location)
    }


    fun begin() {
        checkManaged()
        GLES20.glUseProgram(programHandle)
    }

    fun dispose() {
        GLES20.glUseProgram(0)
        GLES20.glDeleteShader(vertexShaderHandle)
        GLES20.glDeleteShader(fragmentShaderHandle)
        GLES20.glDeleteProgram(programHandle)
        uniforms.clear()
        attributes.clear()
        invalidated = true
    }

    private fun checkManaged() {
        if (invalidated) {
            compileShader()
            invalidated = false
        }
    }

    private val paramsBuffer = ByteBuffer.allocate(4).run {
        order(ByteOrder.nativeOrder())
        asIntBuffer()
    }

    private val typeBuffer = ByteBuffer.allocate(4).run {
        order(ByteOrder.nativeOrder())
        asIntBuffer()
    }

    private fun fetchAttributes() {
        paramsBuffer.clear()
        GLES20.glGetProgramiv(programHandle, GLES20.GL_ACTIVE_ATTRIBUTES, paramsBuffer)
        val count = paramsBuffer.get(0)
        for (i in 0 until count) {
            typeBuffer.clear()
            val name =GLES20.glGetActiveAttrib(programHandle, i, paramsBuffer, typeBuffer)
            val location = GLES20.glGetAttribLocation(programHandle, name)
            attributes[name] = location
        }
    }
    private fun fetchUniforms() {
        paramsBuffer.clear()
        GLES20.glGetProgramiv(programHandle, GLES20.GL_ACTIVE_UNIFORMS, paramsBuffer)
        val count = paramsBuffer.get(0)
        for (i in 0 until count) {
            typeBuffer.clear()
            val name =GLES20.glGetActiveUniform(programHandle, i, paramsBuffer, typeBuffer)
            val location = GLES20.glGetUniformLocation(programHandle, name)
            uniforms[name] = location
        }
    }
}