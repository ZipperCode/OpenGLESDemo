package com.opensource.gradientdemo

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLSurfaceView.Renderer
import com.opensource.gradientdemo.shader.ColorShader
import com.opensource.gradientdemo.shader.GradientShader
import com.opensource.gradientdemo.utils.AssetsUtil
import com.opensource.gradientdemo.utils.OpenGLHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sqrt

/**
 *
 * @author  zhangzhipeng
 * @date    2025/1/6
 */
class GradientRender(
    private val context: Context
) : Renderer {

    private var programId = 0
    private var positionLocation = 0
    private var colorPosition = 0
    private var width = 0
    private var height = 0

    var vertices: FloatArray = floatArrayOf(
        -1f, 1f, 1.0f, 0.0f, 0.0f,  // 左上角，红色
        -1f, -1f, 0.0f, 0.0f, 1.0f,  // 左下角，蓝色
        1f, 1f, 0.0f, 1.0f, 1.0f,  // 右上角，白色
        1f, -1f, 1.0f, 1.0f, 0.0f,    // 左上角，黄色
    )

    private val colorShader = ColorShader(context)
    private val gradientShader = GradientShader(context)

    private val listPoints = mutableListOf<GradientPoint>()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val vert = AssetsUtil.getAssetsContent(context, "point_gradient.vert")
        val frag = AssetsUtil.getAssetsContent(context, "point_gradient.frag")
        programId = OpenGLHelper.createProgram(vert, frag)
        positionLocation = GLES20.glGetAttribLocation(programId, "aPosition")
        colorPosition = GLES20.glGetAttribLocation(programId, "aColor")
//        colorShader.onSurfaceCreate()
        gradientShader.onSurfaceCreate()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(0f, 0f, 0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

//        glUseProgram(programId)
//        OpenGLHelper.createVbo(vertices)
//        glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 5 * Float.SIZE_BYTES, 0)
//        glEnableVertexAttribArray(positionLocation)
//        glVertexAttribPointer(colorPosition, 3, GL_FLOAT, false, 5 * Float.SIZE_BYTES, 2 * Float.SIZE_BYTES)
//        glEnableVertexAttribArray(colorPosition)
//
////        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
//        glDrawArrays(GL_POINTS, 0, 4)
//        colorShader.onDrawFrame(Color.RED)
        if (listPoints.isEmpty()) {
            return
        }
        gradientShader.onDrawFrame(listPoints)

    }

    private val vertArray =  floatArrayOf(
        -1.0f, 1.0f, // 左下
        -1.0f, -1.0f, // 右下
        1.0f, 1.0f, // 右上
        1.0f, -1.0f, // 左上
    )

    fun onTouchDown(x: Float, y: Float, currentColor: Int) {
        val xRatio = x / width
        val yRatio = y / height
        if (listPoints.lastOrNull()?.color == currentColor) {
            return
        }
        val nearestVertex = findNearestVertex(OpenGLHelper.convertGlVertex(xRatio, yRatio), vertArray)

        listPoints.add(GradientPoint(currentColor, nearestVertex))
    }

    fun findNearestVertex(worldPoint: FloatArray, vertices: FloatArray): FloatArray {
        var minDistance = Float.MAX_VALUE
        var nearestVertex = floatArrayOf(0f, 0f)

        for (i in 0 until vertices.size step 2) {
            val vertex = floatArrayOf(vertices[i], vertices[i + 1])
            val distance = calculateDistance(worldPoint, vertex)

            if (distance < minDistance) {
                minDistance = distance
                nearestVertex = vertex
            }
        }

        return nearestVertex
    }

    fun calculateDistance(point1: FloatArray, point2: FloatArray): Float {
        val dx = point1[0] - point2[0]
        val dy = point1[1] - point2[1]
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }


    fun reset() {
        listPoints.clear()
    }
}
