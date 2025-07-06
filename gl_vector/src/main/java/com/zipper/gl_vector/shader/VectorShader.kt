package com.zipper.gl_vector.shader

import com.zipper.gl_vector.GL
import com.zipper.gl_vector.gl.GLTexture

class VectorShader : BaseShader() {

    var glTexture: GLTexture? = null

    override fun render(mvpMatrix: FloatArray) {
        val finalGlTexture = glTexture ?: return
        super.render(mvpMatrix)
        program.setTexture("uTexture", finalGlTexture, 0)
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
uniform sampler2D uTexture;


varying vec2 vTexCoordinate;
float rgbToLuminance(vec3 rgb) {
    return dot(rgb, vec3(0.2126, 0.7152, 0.0722));
}
void main() {
    vec4 color = texture2D(uTexture, vTexCoordinate);
    float luminance = clamp(rgbToLuminance(color.rgb), 0.0, 1.0);
gl_FragColor = luminance == 0.0 ? vec4(0.0, 0.0, 0.0, 1.0) : vec4(0.0, 0.0, 0.0, 1.0 - luminance);
}
"""
    }

}