package com.zipper.gl_vector.shader

import com.zipper.gl_vector.GL
import com.zipper.gl_vector.gl.GLTexture

/**
 *
 * @author  zhangzhipeng
 * @date    2025/7/4
 */
class TextureShader : BaseShader() {

    private var texture: GLTexture? = null

    fun setTexture(texture: GLTexture?) {
        this.texture = texture
    }

    override fun render(mvpMatrix: FloatArray) {
        if (texture == null) {
            return
        }
        super.render(mvpMatrix)
        texture?.activeBind(0)
        program.setUniform1i("uTexture", 0)
        GL.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4)
    }

    override fun getVertexShaderCode(): String {
        return """
precision highp float;

attribute vec4 aPosition;
attribute vec2 aTextureCoordinate;
uniform mat4 uMatrix;

varying vec2 vTexCoordinate;

void main() {
    gl_Position = uMatrix * aPosition;
    vTexCoordinate = aTextureCoordinate;
}
"""
    }

    override fun getFragmentShaderCode(): String {
        return """
precision mediump float;

uniform sampler2D uTexture;

varying vec2 vTexCoordinate;

void main() {
    vec4 color = texture2D(uTexture, vTexCoordinate);
    gl_FragColor = color;
}
"""
    }


}