package com.zipper.gl_vector.gl

import android.graphics.Bitmap
import android.opengl.GLUtils
import com.zipper.gl_vector.GL

open class GLTexture(
    private val glTarget: Int = GL.GL_TEXTURE_2D
) {

    private var glHandle: Int = 0

    val texture: Int get() = glHandle

    fun init() {
        if (glHandle == 0) {
//            val buf = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer()
//            GL.glGenTextures(1, buf)
//            glHandle = buf.get(0)
            val texture = IntArray(1)
            // 生成纹理id
            GL.glGenTextures(1, texture, 0)
            glHandle = texture[0]
        }
    }

    fun bind() {
        if (glHandle != 0) {
            GL.glBindTexture(glTarget, glHandle)
        }
    }

    fun activeBind(index: Int) {
        GL.glActiveTexture(GL.GL_TEXTURE0 + index)
        bind()
    }

    fun unbind() {
        GL.glBindTexture(glTarget, 0)
    }

    fun dispose() {
        if (glHandle != 0) {
            val texture = intArrayOf(glHandle)
            GL.glDeleteTextures(1, texture, 0)
            glHandle = 0
        }
    }

    fun setFilter(min: Int = GL.GL_LINEAR, mag: Int = GL.GL_LINEAR) = apply {
        // 最近采样： 使用纹理坐标最接近的颜色作为需要绘制的颜色 (容易带锯齿)
        // 线性采样器：使用纹理坐标 附近的若干个颜色，加权平均 得到需要绘制的颜色 (平滑)
        // 设置最小过滤器 为 最近采样： 使用纹理坐标最接近的颜色作为需要绘制的颜色
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, min)
        // 设置最大功过滤器 为 线性采样器：使用纹理坐标 附近的若干个颜色，加权平均 得到需要绘制的颜色
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, mag)
    }

    fun setWrap(wrapS: Int = GL.GL_CLAMP_TO_EDGE, wrapT: Int = GL.GL_CLAMP_TO_EDGE) = apply {
        // 设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, wrapS)
        // 设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, wrapT)
    }

    fun upload(bitmap: Bitmap) {
        init()
        bind()
        setFilter().setWrap()
        GLUtils.texImage2D(glTarget, 0, bitmap, 0)
        unbind()
    }

    fun texImage2D(width: Int, height: Int, format: Int = GL.GL_RGBA, type: Int = GL.GL_UNSIGNED_BYTE) {
        GL.glTexImage2D(glTarget, 0, GL.GL_RGBA, width, height, 0, format, type, null)
    }
}