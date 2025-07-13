package com.zipper.gldemo.pen.shader

import android.content.Context
import android.util.Log
import com.zipper.gl.base.GL
import com.zipper.gl.base.GLTexture
import com.zipper.gl.base.GLVertexBuffer
import com.zipper.gl.base.ShaderProgram
import com.zipper.gldemo.pen.BrushPointInfo

class BrushShader(private val context: Context) {

    companion object {

        const val VERTEX_SIZE = 6

        const val VERTEX_POS_LOC = 0
        const val VERTEX_POINT_SIZE_LOC = 2
        const val VERTEX_COLOR_LOC = 3
    }

    private val capacity = 512

    private var vertexSize = 0

    private val tempVertex = FloatArray(VERTEX_SIZE)

    private val glVertexBuffer = GLVertexBuffer(FloatArray(VERTEX_SIZE * capacity))

    private val program = ShaderProgram(getVertexCode(), getFragmentCode())

    val brushTexture = GLTexture()

    /**
     * 顶点绘制缓冲
     */
    private var inActivePointBuffer = mutableListOf<BrushPointInfo>()
    private var activePointBuffer = mutableListOf<BrushPointInfo>()

    fun initialize() {
        program.initialize()
    }

    fun render(fboRatio: Float, modelRatio: Float): Boolean {
        togglePointBuffer()
        if (activePointBuffer.isEmpty()) {
            Log.d("BAAA", "no active point")
            return false
        }
        val vertexSize = minOf(activePointBuffer.size, capacity)
        Log.d("BAAA", "render points = $vertexSize")
        program.useProgram()
        program.setUniform1f("uFboRatio", fboRatio)
        program.setUniform1f("uModelRatio", modelRatio)
        GL.glActiveTexture(GL.GL_TEXTURE0)
        brushTexture.bind()
        program.setTexture("uBrushTexture", brushTexture, 0)
        configVertex()
        GL.glDrawArrays(GL.GL_POINTS, 0, vertexSize)
        program.useProgram(false)
        return true
    }

    fun addPoint(record: BrushPointInfo) {
        inActivePointBuffer.add(0, record)
    }

    fun syncVertexBuffer() {
        glVertexBuffer.reset()
        for (index in 0 until minOf(inActivePointBuffer.size, capacity)) {
            updateVertex(index, inActivePointBuffer[index])
        }
    }

    private fun togglePointBuffer() {
        val tempList = activePointBuffer
        activePointBuffer = inActivePointBuffer
        inActivePointBuffer = tempList
        inActivePointBuffer.clear()
    }

    private fun configVertex() {
        val posLoc = program.fetchAttributeLocation("aPosition")
        glVertexBuffer.vertexAttrPointer(posLoc, VERTEX_POS_LOC, 2, VERTEX_SIZE * 4)
        val pointSizeLoc = program.fetchAttributeLocation("aPointSize")
        glVertexBuffer.vertexAttrPointer(pointSizeLoc, VERTEX_POINT_SIZE_LOC, 1, VERTEX_SIZE * 4)
        val colorLoc = program.fetchAttributeLocation("aColor")
        glVertexBuffer.vertexAttrPointer(colorLoc, VERTEX_COLOR_LOC, 3, VERTEX_SIZE * 4)
    }

    private fun updateVertex(index: Int, record: BrushPointInfo) {
        val dataIndex = index * VERTEX_SIZE
        var index = 0
        tempVertex[index++] = record.point.x
        tempVertex[index++] = record.point.y
        tempVertex[index++] = record.brushSize
        tempVertex[index++] = record.color[0]
        tempVertex[index++] = record.color[1]
        tempVertex[index++] = record.color[2]
        glVertexBuffer.putVertex(tempVertex, dataIndex, VERTEX_SIZE)
    }

    private fun getVertexCode(): String {
        return context.assets.open("brush.vert").bufferedReader().readText()
    }

    private fun getFragmentCode(): String {
        return context.assets.open("brush.frag").bufferedReader().readText()
    }
}