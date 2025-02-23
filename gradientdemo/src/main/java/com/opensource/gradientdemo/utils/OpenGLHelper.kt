package com.opensource.gradientdemo.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

object OpenGLHelper {
    @JvmStatic
    fun createProgram(
        vertexSource: String,
        fragmentSource: String,
    ): Int {
        val vertexShader = createShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = createShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (vertexShader == 0 || fragmentShader == 0) {
            Log.e("OpenGLDemo", "createProgram 失败，vertexShader = $vertexShader fragmentShader = $fragmentShader")
            return 0
        }
        // 创建程序
        var program = GLES20.glCreateProgram()
        // 绑定顶点着色器
        GLES20.glAttachShader(program, vertexShader)
        // 绑定片元着色器
        GLES20.glAttachShader(program, fragmentShader)
        // 链接程序
        GLES20.glLinkProgram(program)
        val linkStatus = intArrayOf(0)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val info = GLES20.glGetProgramInfoLog(program)
            GLES20.glDeleteProgram(program)
            Log.e("OpenGLDemo", "Could not link program:$info")
            program = 0
        }
        return program
    }

    @JvmStatic
    fun createShader(
        type: Int,
        source: String,
    ): Int {
        // 创建shader
        var shader = GLES20.glCreateShader(type)
        // 加载源代码
        GLES20.glShaderSource(shader, source)
        // 编译源码
        GLES20.glCompileShader(shader)
        val compiled = intArrayOf(0)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val info = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            Log.e("OpenGLDemo", "Could not compile shader $type:$info")
            shader = 0
        }
        return shader
    }

    fun createTexTexture(bitmap: Bitmap?): Int {
        if (bitmap == null || bitmap.isRecycled) {
            Log.e("OpenGLDemo", "创建纹理失败，bitmap为空")
            return 0
        }
        val texture = IntArray(1)
        // 生成纹理id
        GLES20.glGenTextures(1, texture, 0)
        val textureId = texture[0]
        // 绑定纹理id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        
        // 设置纹理参数
        // 使用线性过滤，并开启mipmap
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        
        // 设置环绕方式为CLAMP，防止边缘重复
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        
        // 生成2D纹理
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        // 生成mipmap
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        
        return textureId
    }

    fun createNearestTexture(bitmap: Bitmap?): Int {
        if (bitmap == null || bitmap.isRecycled) {
            return 0
        }
        val texture = IntArray(1)
        // 生成纹理id
        GLES20.glGenTextures(1, texture, 0)
        val textureId = texture[0]
        // 绑定纹理id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        // 设置纹理参数

        // 设置最小过滤器 为 最近采样： 使用纹理坐标最接近的颜色作为需要绘制的颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        // 设置最大功过滤器 为 线性采样器：使用纹理坐标 附近的若干个颜色，加权平均 得到需要绘制的颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST.toFloat())

        // 设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        // 设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        // 根据以上指定的参数，生成一个2D纹理
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textureId
    }

    fun createFBOTexture(width: Int, height: Int): Int {
        val arr = IntArray(1)
        // 创建纹理
        GLES20.glGenTextures(1, arr, 0)

        val textureId = arr[0]
        if (textureId <= 0) {
            return 0
        }

        // 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, arr[0])

        // 设置最小过滤器 为 最近采样： 使用纹理坐标最接近的颜色作为需要绘制的颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        // 设置最大功过滤器 为 线性采样器：使用纹理坐标 附近的若干个颜色，加权平均 得到需要绘制的颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())

        // 设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        // 设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        // 根据以上指定的参数，生成一个2D纹理

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)

        return textureId
    }

    /**
     * 半透明混合
     */
    fun halfAlphaBlend() {
        // 关键步骤，启用颜色混合
        GLES20.glEnable(GLES20.GL_BLEND)
        // 设置混合因子 目标颜色的透明度 = 1 - 源颜色的透明度，此时，如果绘制物体的透明度小于1，则会绘制为透明色
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    /**
     * 将MotionEvent坐标系转化为OpenGL坐标系
     * 加减去 0.5表示将坐标调整到中心，并乘以2，实现 -1, 1 的坐标系转变
     */
    fun convertGlVertex(offsetXRatio: Float, offsetYRatio: Float): FloatArray {
        return floatArrayOf((offsetXRatio - 0.5f) * 2f, (-offsetYRatio + 0.5f) * 2f)
    }

    fun convertGlXVertex(offsetXRatio: Float): Float {
        return (offsetXRatio - 0.5f) * 2f
    }

    fun convertedGlYVertex(offsetYRatio: Float): Float {
        return -(offsetYRatio + 0.5f) * 2f
    }

    fun createFloatBuffer(toFloatArray: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(toFloatArray.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(toFloatArray)
                position(0)
            }
    }

    fun convertColor(color: Int, colorArr: FloatArray) {
        colorArr[0] = Color.red(color) / 255.0f
        colorArr[1] = Color.green(color) / 255.0f
        colorArr[2] = Color.blue(color) / 255.0f
        colorArr[3] = Color.alpha(color) / 255.0f
    }

    fun createVbo(arr: FloatArray) {
        val vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, arr.size * Float.SIZE_BYTES, createFloatBuffer(arr), GLES20.GL_STATIC_DRAW)
    }
}
