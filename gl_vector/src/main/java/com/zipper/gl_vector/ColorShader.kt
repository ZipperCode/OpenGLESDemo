package com.zipper.gl_vector

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ColorShader {

    companion object {
        const val VERTEX_SHADER = """
                        precision highp float;
            attribute vec4 aPosition;
            uniform mat4 uMatrix;
            void main() {
                gl_Position = uMatrix * aPosition;
            }
        """

        const val FRAGMENT_SHADER = """
                        precision highp float;
            uniform vec4 uColor;
            void main() {
                gl_FragColor = uColor;
            }
        """
    }


    private val colorArr = floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f)

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

    fun draw(matrix: FloatArray) {
        program.useProgram()
        program.setMatrix("uMatrix", matrix)
        program.setVertexAttribute("aPosition", 2, GL.GL_FLOAT, false, 0, vertexBuffer.position(0))
        // program.setVertexAttribute("aTextureCoordinate", 2, GL.GL_FLOAT, false, 0, textureBuffer.position(0))
        GL.glActiveTexture(GL.GL_TEXTURE0)
        program.glUniform4fv("uColor", colorArr)
        GL.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4)
//        program.disableVertexAttribute("aPosition")
//        program.disableVertexAttribute("aTextureCoordinate")
//        program.useProgram(false)
    }
}