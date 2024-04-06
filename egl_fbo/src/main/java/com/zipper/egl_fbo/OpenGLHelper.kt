package com.zipper.egl_fbo

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log

object OpenGLHelper {
    @JvmStatic
    fun createProgram(
        vertexSource: String,
        fragmentSource: String,
    ): Int {
        val vertexShader = createShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = createShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        if (vertexShader == 0 || fragmentShader == 0) {
            Log.e("OpenGLDemo", "createProgram 失败，vertexShader = $vertexShader fragmentShader = $fragmentShader")
            return 0
        }
        // 创建程序
        var program = GLES30.glCreateProgram()
        // 绑定顶点着色器
        GLES30.glAttachShader(program, vertexShader)
        // 绑定片元着色器
        GLES30.glAttachShader(program, fragmentShader)
        // 链接程序
        GLES30.glLinkProgram(program)
        val linkStatus = intArrayOf(0)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val info = GLES30.glGetProgramInfoLog(program)
            GLES30.glDeleteProgram(program)
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
        var shader = GLES30.glCreateShader(type)
        // 加载源代码
        GLES30.glShaderSource(shader, source)
        // 编译源码
        GLES30.glCompileShader(shader)
        val compiled = intArrayOf(0)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val info = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            Log.e("OpenGLDemo", "Could not compile shader $type:$info")
            shader = 0
        }
        return shader
    }

    fun createTexture(bitmap: Bitmap?): Int {
        if (bitmap == null || bitmap.isRecycled) {
            Log.e("OpenGLDemo", "创建纹理失败，bitmap为空")
            return 0
        }
        val texture = IntArray(1)
        // 生成纹理id
        GLES30.glGenTextures(1, texture, 0)
        val textureId = texture[0]
        // 绑定纹理id
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        // 设置纹理参数

        // 设置最小过滤器 为 最近采样： 使用纹理坐标最接近的颜色作为需要绘制的颜色
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST.toFloat())
        // 设置最大功过滤器 为 线性采样器：使用纹理坐标 附近的若干个颜色，加权平均 得到需要绘制的颜色
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())

        // 设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        // 设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        // 根据以上指定的参数，生成一个2D纹理
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

        return textureId
    }

    /**
     * 创建一个空白纹理
     */
    fun createEmptyTexture(width: Int, height: Int): Int {
        val arr = IntArray(1)
        // 创建纹理
        GLES30.glGenTextures(1, arr, 0)

        val textureId = arr[0]
        if (textureId <= 0) {
            return 0
        }

        // 绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, arr[0])

        // 设置最小过滤器 为 最近采样： 使用纹理坐标最接近的颜色作为需要绘制的颜色
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat())
        // 设置最大功过滤器 为 线性采样器：使用纹理坐标 附近的若干个颜色，加权平均 得到需要绘制的颜色
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())

        // 设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        // 设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        // 根据以上指定的参数，生成一个2D纹理

        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)

        return textureId
    }

    fun frameBufferTexture2D(textureId: Int, frameBuffer: Int) {
        // 绑定到帧缓冲区
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer)
        // 将纹理内容附着在到帧缓冲区
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, textureId, 0)
        // 取消绑定帧缓冲区
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE)
    }
}