package com.zipper.gl_vector

import android.opengl.GLES20
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder


val VERTEX = floatArrayOf(
    -1.0f, 1.0f, // 左上角,
    -1.0f, -1.0f, // 左下角,
    1.0f, 1.0f, // 右上角,
    1.0f, -1.0f // 右下角,
)

val TEXTURE = floatArrayOf(
    0.0f, 0.0f, // 左上角,
    0.0f, 1.0f, // 左下角,
    1.0f, 0.0f, // 右上角,
    1.0f, 1.0f // 右下角,
)

class TextureShader {

    companion object {
        private const val VERTEX_SHADER = """
precision highp float;

attribute vec4 aPosition;
attribute vec2 aTextureCoordinate;
uniform mat4 uMatrix;

varying vec2 vTexCoordinate;

void main() {
    gl_Position = uMatrix * aPosition;
    vTexCoordinate = aTextureCoordinate;
}
"""

        private const val FRAGMENT_SHADER = """
precision mediump float;

uniform sampler2D uTexture;

varying vec2 vTexCoordinate;

void main() {
    vec4 color = texture2D(uTexture, vTexCoordinate);
    gl_FragColor = color;
}
"""
    }

    private val program by lazy {
        ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER)
    }

    private val vertexBuffer = ByteBuffer.allocateDirect(VERTEX.size * 4).order(ByteOrder.nativeOrder())
        .asFloatBuffer().put(VERTEX).position(0)
    private val textureBuffer = ByteBuffer.allocateDirect(TEXTURE.size * 4).order(ByteOrder.nativeOrder())
        .asFloatBuffer().put(TEXTURE).position(0)

    fun initialize() {
        program.initialize()
    }

    fun draw(texture: GLTexture, matrix: FloatArray) {
        program.useProgram()
        program.setMatrix("uMatrix", matrix)
        program.setVertexAttribute("aPosition", 2, GL.GL_FLOAT, false, 0, vertexBuffer.position(0))
        program.setVertexAttribute("aTextureCoordinate", 2, GL.GL_FLOAT, false, 0, textureBuffer.position(0))
        texture.activeBind(0)
        program.setUniform1i("uTexture", 0)
        GL.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4)
        program.disableVertexAttribute("aPosition")
        program.disableVertexAttribute("aTextureCoordinate")
        program.useProgram(false)
    }

    fun dispose() {
        program.dispose()
    }
}