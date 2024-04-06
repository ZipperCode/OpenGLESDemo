package com.zipper.egl_fbo

import android.content.Context
import android.opengl.GLES30

class ImageShader(context: Context) : BaseShader() {

    override fun onSurfaceCreate() {
        super.onSurfaceCreate()
    }


    fun draw(textureHandle: Int) {

        GLES30.glUseProgram(programHandle[0])
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glEnableVertexAttribArray(1)

        // 设置顶点和纹理坐标
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 0, positionBuffer)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, textureCoordinateBuffer)
        // 激活纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        // 绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle)
        // 绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)

    }

    override fun getVertexShaderCode(): String {
        return """
            #version 300 es
            layout (location = 0) in vec4 vPosition;
            layout (location = 1) in vec2 aTextureCoord;
            out vec2 vTexCoord;
            void main() {
                gl_Position  = vPosition;
                vTexCoord = aTextureCoord;
            }
        """.trimIndent()
    }

    override fun getFragmentShaderCode(): String {
        return """
            #version 300 es
            precision mediump float;
            uniform sampler2D uTextureUnit;
            in vec2 vTexCoord;
            out vec4 fragColor;
            void main() {
                vec4 color = texture(uTextureUnit, vTexCoord);
                float rgb = color.g;
                vec4 c = vec4(rgb, rgb, rgb, color.a);
                fragColor = c;
            }

        """.trimIndent()
    }
}