package com.zipper.gl.base

import java.nio.ByteBuffer
import java.nio.ByteOrder

class GLVertexBuffer(vertex: FloatArray) {

    private val buffer = ByteBuffer.allocateDirect(vertex.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertex)

    init {
        buffer.position(0)
    }


    fun reset() {
        buffer.clear()
    }

    fun vertexAttrPointer(loc: Int, pos: Int, size: Int, stride: Int) {
        buffer.position(pos)
        GL.glEnableVertexAttribArray(loc)
        GL.glVertexAttribPointer(loc, size, GL.GL_FLOAT, false, stride, buffer)
        buffer.position(0)
    }

    fun putVertex(vertex: FloatArray, pos: Int, count: Int) {
        buffer.position(pos)
        buffer.put(vertex, 0, count)
        buffer.position(0)
    }

}