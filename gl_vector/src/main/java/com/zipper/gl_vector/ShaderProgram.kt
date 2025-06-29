package com.zipper.gl_vector

import android.util.Log
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

open class ShaderProgram(
    private val vertexShaderSource: String,
    private val fragmentShaderSource: String
) {

    /**
     * 是否编译
     */
    var isCompiled = false
        private set

    private val uniforms = mutableMapOf<String, Int>()

    private val attributes = mutableMapOf<String, Int>()

    private var program = -1

    private var vertexShaderHandle = -1
    private var fragmentShaderHandle = -1

    private var invalidated = false

    private var log = ""

    fun initialize() {
        compileShaders()
        if (isCompiled) {
            fetchAttributes()
            fetchUniforms()
        }
        Log.e("BAAA", "err = $log")
    }

    fun useProgram(enable: Boolean = true) {
        if (enable) {
            GL.glUseProgram(program)
        } else {
            GL.glUseProgram(0)
        }
    }

    fun dispose() {
        GL.glUseProgram(0)
        GL.glDeleteShader(vertexShaderHandle)
        GL.glDeleteShader(fragmentShaderHandle)
        GL.glDeleteProgram(program)
        vertexShaderHandle = -1
        fragmentShaderHandle = -1
        program = -1
    }

    fun fetchAttributeLocation(name: String): Int {
        checkManaged()
        var location = attributes.getOrDefault(name, -2)
        if (location == -2) {
            location = GL.glGetAttribLocation(program, name)
            if (location == 0 || location == -1) {
                throw IllegalArgumentException("Attribute $name not found")
            }
            attributes[name] = location
        }
        return location
    }

    fun fetchUniformLocation(name: String): Int {
        checkManaged()
        var location = uniforms.getOrDefault(name, -2)
        if (location == -2) {
            location = GL.glGetUniformLocation(program, name)
            if (location == 0 || location == -1) {
                throw IllegalArgumentException("Uniform $name not found")
            }
            uniforms[name] = location
        }
        return location
    }

    fun setMatrix(name: String, matrix: FloatArray) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GL.glUniformMatrix4fv(location, 1, false, matrix, 0)
    }

    fun enableVertexAttribute(name: String) {
        checkManaged()
        val location = fetchAttributeLocation(name)
        GL.glEnableVertexAttribArray(location)
    }

    fun disableVertexAttribute(name: String) {
        checkManaged()
        val location = fetchAttributeLocation(name)
        GL.glDisableVertexAttribArray(location)
    }

    fun setVertexAttribute(name: String, size: Int, type: Int, normalized: Boolean, stride: Int, buffer: Buffer) {
        checkManaged()
        val location = fetchAttributeLocation(name)
        GL.glEnableVertexAttribArray(location)
        GL.glVertexAttribPointer(location, size, type, normalized, stride, buffer)
    }

    fun setUniform1i(name: String, value: Int) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GL.glUniform1i(location, value)
    }

    fun glUniform4fv(name: String, value: FloatArray) {
        checkManaged()
        val location = fetchUniformLocation(name)
        GL.glUniform4fv(location, 1, value, 0)
    }

    private fun compileShaders() {
        vertexShaderHandle = loadShader(GL.GL_VERTEX_SHADER, vertexShaderSource)
        fragmentShaderHandle = loadShader(GL.GL_FRAGMENT_SHADER, fragmentShaderSource)
        if (vertexShaderHandle == -1 || fragmentShaderHandle == -1) {
            isCompiled = false
            return
        }
        program = createProgram()
        if (program == -1) {
            isCompiled = false
            return
        }
        isCompiled = true
    }

    private fun checkManaged() {
        if (invalidated) {
            compileShaders()
            invalidated = false
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val intBuf = newIntBuffer(1)
        val shader = GL.glCreateShader(type)
        if (shader == 0) {
            return -1
        }
        GL.glShaderSource(shader, shaderCode)
        GL.glCompileShader(shader)
        GL.glGetShaderiv(shader, GL.GL_COMPILE_STATUS, intBuf)
        val compiled = intBuf[0]
        if (compiled == 0) {
            val info = GL.glGetShaderInfoLog(shader)
            log += "${if (type == GL.GL_VERTEX_SHADER) "Vertex" else "Fragment"} shader:\n"
            log += info
            return -1
        }
        return shader
    }

    private fun createProgram(): Int {
        val program = GL.glCreateProgram()
        if (program == -1) {
            return -1
        }
        GL.glAttachShader(program, vertexShaderHandle)
        GL.glAttachShader(program, fragmentShaderHandle)
        GL.glLinkProgram(program)

        val tmp = newIntBuffer(1)
        GL.glGetProgramiv(program, GL.GL_LINK_STATUS, tmp)
        val linked = tmp.get(0)
        if (linked == 0) {
            log += GL.glGetProgramInfoLog(program)
            return -1
        }
        return program
    }

    private val param = newIntBuffer(1)
    private val type = newIntBuffer(1)

    private fun fetchAttributes() {
        param.clear()
        GL.glGetProgramiv(program, GL.GL_ACTIVE_ATTRIBUTES, param)
        val numAttributes = param.get(0)
        for (i in 0 until numAttributes) {
            param.clear()
            param.put(0, 1)
            type.clear()
            val name = GL.glGetActiveAttrib(program, i, param, type)
            val location = GL.glGetAttribLocation(program, name)
            attributes[name] = location
        }
    }

    private fun fetchUniforms() {
        param.clear()
        GL.glGetProgramiv(program, GL.GL_ACTIVE_UNIFORMS, param)
        val numUniforms = param.get(0)
        for (i in 0 until numUniforms) {
            param.clear()
            param.put(0, 1)
            type.clear()
            val name = GL.glGetActiveUniform(program, i, param, type)
            val location = GL.glGetUniformLocation(program, name)
            uniforms[name] = location
        }
    }

    private fun newIntBuffer(numInts: Int): IntBuffer {
        val buffer = ByteBuffer.allocateDirect(numInts * 4)
        buffer.order(ByteOrder.nativeOrder())
        return buffer.asIntBuffer()
    }
}