package com.zipper.gldemo2

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRender(
    private val context: Context
) : GLSurfaceView.Renderer {

    private val programHandle = IntArray(1)
    private var positionLocation = -1
    private var coordinateLocation = -1
    private var textureLocation = -1


    private val vertexPositions = floatArrayOf(
        -1.0f, 1.0f, // 左下
        -1.0f, -1.0f, // 右下
        1.0f, 1.0f, // 右上
        1.0f, -1.0f, // 左上
    )

    private val textureCoordinate = floatArrayOf(
        0.0f, 0f, // 圆点 0,0
        0.0f, 1.0f, // 0,1
        1.0f, 0f, // 1,0
        1.0f, 1f, // 1,1
    )

    private val positionBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertexPositions.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
            put(vertexPositions)
            position(0)
        }

    private val textureCoordinateBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(textureCoordinate.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(textureCoordinate)
                position(0)
            }


    private val lineBitmap = AssetsUtil.decodeBitmapFromAssets(context, "903388/svg2png.png")

    private var lineTextureId = -1

    private val textureShader = TextureShader()

    private val lineShader = LineShader(context)

    private var orgTextureId = -1

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//        programHandle[0] = OpenGLHelper.createProgram(getVertexShaderCode(), getFragmentShaderCode())
//        positionLocation = GLES20.glGetAttribLocation(programHandle[0], "vPosition")
//        coordinateLocation = GLES20.glGetAttribLocation(programHandle[0], "vTextureCoordinate")
//        textureLocation = GLES20.glGetUniformLocation(programHandle[0], "uTexture")
//
//        lineTextureId = OpenGLHelper.createTexTexture(lineBitmap)
        lineShader.onSurfaceCreate()

        val bitmap = AssetsUtil.decodeBitmapFromAssets(context, "903388/org.png")
        orgTextureId = OpenGLHelper.createTexTexture(bitmap)
        bitmap?.recycle()
        textureShader.onSurfaceCreate()

        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        textureShader.onDrawFrame(orgTextureId)

        lineShader.onDrawFrame()

//        GLES20.glUseProgram(programHandle[0])
//        GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 0, positionBuffer)
//        GLES20.glEnableVertexAttribArray(positionLocation)
//        GLES20.glEnable(GLES20.GL_BLEND);
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//        GLES20.glVertexAttribPointer(coordinateLocation, 2, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer)
//        GLES20.glEnableVertexAttribArray(coordinateLocation)
//        Log.d("BAAA", "err = ${GLES20.glGetError()}")
//
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, lineTextureId)
//        GLES20.glUniform1i(textureLocation, 0)
//
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
//
//        GLES20.glDisableVertexAttribArray(positionLocation)
//        GLES20.glDisableVertexAttribArray(coordinateLocation)
////        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
//        GLES20.glUseProgram(0)
    }

    private fun getVertexShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gles/line_shader.vert")
    }

    private fun getFragmentShaderCode(): String {
        return AssetsUtil.getAssetsContent(context, "gles/line_shader.frag")
    }
}