package com.zipper.gldemo.pen.shader

import android.content.Context
import com.zipper.gl.base.GL
import com.zipper.gl.base.GLTexture
import com.zipper.gl.base.GLVertexBuffer
import com.zipper.gl.base.ShaderProgram
import com.zipper.gldemo.pen.BrushPointInfo
import com.zipper.gldemo.pen.BrushVertex

class BrushShader(private val context: Context) {

    companion object {

        const val VERTEX_SIZE = 7

        const val VERTEX_POS_LOC = 0
        const val VERTEX_ANGLE_LOC = 2
        const val VERTEX_POINT_SIZE_LOC = 3
        const val VERTEX_COLOR_LOC = 4

        const val CAPACITY = 512
    }

    private val tempVertex = FloatArray(VERTEX_SIZE)

    private val glVertexBuffer = GLVertexBuffer(FloatArray(VERTEX_SIZE * CAPACITY))

    private val program = ShaderProgram(getVertexCode(), getFragmentCode())

    private val pendingRenderBuffer = mutableListOf<BrushVertex>()

    fun initialize() {
        program.initialize()
        glVertexBuffer.reset()
        pendingRenderBuffer.clear()
    }

    fun draw(fboRatio: Float, modelRatio: Float, brushTexture: GLTexture) {
        if (pendingRenderBuffer.isEmpty()) {
            return
        }
        program.useProgram()
        program.setUniform1f("uViewRatio", fboRatio)
        program.setUniform1f("uModelSize", modelRatio)
        program.setTexture("uBrushTexture", brushTexture, 0)
        drawPoints()
        program.useProgram(false)
    }

    private fun drawPoints() {
        val count = pendingRenderBuffer.size / CAPACITY
        val mod = pendingRenderBuffer.size % CAPACITY
        // Log.d("BAAA", "count = $count mod = $mod")
        for (index in 0 until count) {
            glVertexBuffer.reset()
            val start = index * CAPACITY
            val end = start + CAPACITY
            for (i in start until end) {
                uploadVertex(i - start, pendingRenderBuffer[i])
            }
            configVertex()
            GL.glDrawArrays(GL.GL_POINTS, 0, CAPACITY)
        }
        glVertexBuffer.reset()
        val modIndex = count * CAPACITY
        for (i in 0 until mod) {
            uploadVertex(i, pendingRenderBuffer[modIndex + i])
        }
        configVertex()
        GL.glDrawArrays(GL.GL_POINTS, 0, mod)
        for (i in pendingRenderBuffer.size - 1 downTo 0) {
            pendingRenderBuffer.removeAt(i).recycle()
        }
    }

    fun addPoint(record: BrushVertex) {
        pendingRenderBuffer.add(0, record)
    }

    fun cleanPoints() {
        pendingRenderBuffer.clear()
    }


    private fun configVertex() {
        val posLoc = program.fetchAttributeLocation("aPosition")
        glVertexBuffer.vertexAttrPointer(posLoc, VERTEX_POS_LOC, 2, VERTEX_SIZE * 4)
        val angleLoc = program.fetchAttributeLocation("aAngle")
        glVertexBuffer.vertexAttrPointer(angleLoc, VERTEX_ANGLE_LOC, 1, VERTEX_SIZE * 4)
        val pointSizeLoc = program.fetchAttributeLocation("aPointSize")
        glVertexBuffer.vertexAttrPointer(pointSizeLoc, VERTEX_POINT_SIZE_LOC, 1, VERTEX_SIZE * 4)
        val colorLoc = program.fetchAttributeLocation("aColor")
        glVertexBuffer.vertexAttrPointer(colorLoc, VERTEX_COLOR_LOC, 3, VERTEX_SIZE * 4)

    }

    private fun uploadVertex(index: Int, record: BrushVertex) {
        val dataIndex = index * VERTEX_SIZE
        var index = 0
        tempVertex[index++] = record.vertexX
        tempVertex[index++] = record.vertexY
        tempVertex[index++] = record.angle
        tempVertex[index++] = record.brushSize
        tempVertex[index++] = record.r
        tempVertex[index++] = record.g
        tempVertex[index++] = record.b
        glVertexBuffer.putVertex(tempVertex, dataIndex, VERTEX_SIZE)
    }

    private fun getVertexCode(): String {
        return context.assets.open("brush.vert").bufferedReader().readText()
    }

    private fun getFragmentCode(): String {
        return context.assets.open("brush.frag").bufferedReader().readText()
    }
}