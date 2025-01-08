package com.zipper.gldemo2.gradient

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView.Renderer
import com.zipper.gldemo2.AssetsUtil
import com.zipper.gldemo2.OpenGLHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GradientRender(
    private val context: Context
) : Renderer {

    private var programId = 0
    private var resolutionLocation = 0
    private var pointCountLocation = 0
    private var pointsLocation = 0
    private var colorsLocation = 0
    private var gColorLocation = 0
    private var gPointLocation = 0

    private val maxPoints = 4
    private val points = FloatArray(maxPoints * 2)  // x,y for each point
    private val colors = FloatArray(maxPoints * 3) // r,g,b for each point
    private var pointCount = 0

    private var width = 1
    private var height = 1

    private var vertexBufferId = 0
    private val quadVertices = floatArrayOf(
        -1f, -1f,  // bottom left
        1f, -1f,   // bottom right
        -1f, 1f,   // top left
        1f, 1f     // top right
    )

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val vert = AssetsUtil.getAssetsContent(context, "gles/gradient.vert")
        val frag = AssetsUtil.getAssetsContent(context, "gles/gradient.frag")
        programId = OpenGLHelper.createProgram(vert, frag)
        
        resolutionLocation = glGetUniformLocation(programId, "uResolution")
        pointCountLocation = glGetUniformLocation(programId, "uPointCount")
        pointsLocation = glGetUniformLocation(programId, "uPoints")
        colorsLocation = glGetUniformLocation(programId, "uColors")
        gColorLocation = glGetUniformLocation(programId, "uGColor")
        gPointLocation = glGetUniformLocation(programId, "uGPoint")
        
        // Create vertex buffer
        val buffers = IntArray(1)
        glGenBuffers(1, buffers, 0)
        vertexBufferId = buffers[0]
        
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId)
        glBufferData(
            GL_ARRAY_BUFFER,
            quadVertices.size * Float.SIZE_BYTES,
            OpenGLHelper.createFloatBuffer(quadVertices),
            GL_STATIC_DRAW
        )
        
        // Set up vertex attribute
        val positionLocation = glGetAttribLocation(programId, "aPosition")
        glEnableVertexAttribArray(positionLocation)
        glVertexAttribPointer(
            positionLocation,
            2,
            GL_FLOAT,
            false,
            2 * Float.SIZE_BYTES,
            0
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        this.width = width
        this.height = height
        glUseProgram(programId)
        glUniform2f(resolutionLocation, width.toFloat(), height.toFloat())
    }

    override fun onDrawFrame(gl: GL10?) {
        glClearColor(0f, 0f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)

        glUseProgram(programId)
        
        // Pass points and colors to shader
        glUniform1i(pointCountLocation, pointCount)
        glUniform2fv(pointsLocation, pointCount, points, 0)
        glUniform3fv(colorsLocation, pointCount, colors, 0)
        
        // Set third point if exists
        if (pointCount > 2) {
            glUniform4f(gColorLocation, 
                colors[6], colors[7], colors[8], 1.0f)
            glUniform2f(gPointLocation, 
                points[4], points[5])
        }
        
        // Draw full screen quad
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
    }

    fun onTouchDown(x: Float, y: Float, color: Int) {
        if (pointCount >= maxPoints) return
        
        // Convert screen coordinates to OpenGL coordinates
        val glX = (x / width) * 2 - 1
        val glY = 1 - (y / height) * 2
        
        // Store point
        points[pointCount * 2] = glX
        points[pointCount * 2 + 1] = glY
        
        // Store color (convert from 0xAARRGGBB to float[3])
        colors[pointCount * 3] = ((color shr 16) and 0xFF) / 255f
        colors[pointCount * 3 + 1] = ((color shr 8) and 0xFF) / 255f
        colors[pointCount * 3 + 2] = (color and 0xFF) / 255f
        
        pointCount++
    }
}
