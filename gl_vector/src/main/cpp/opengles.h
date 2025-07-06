//
// Created by Zipper on 2025/7/6.
//

#ifndef OPENGLEDEMO_OPENGLES_H
#define OPENGLEDEMO_OPENGLES_H

#include <jni.h>
#include <GLES3/gl3.h>
#include <EGL/egl.h>

// 顶点数据 (一个全屏的四边形)
const GLfloat VERTICES[] = {
        -1.0f, -1.0f, // bottom-left
        1.0f, -1.0f, // bottom-right
        -1.0f, 1.0f, // top-left
        1.0f, 1.0f  // top-right
};

// 纹理坐标 (对应全屏四边形)
const GLfloat TEX_COORDS[] = {
        0.0f, 0.0f, // bottom-left
        1.0f, 0.0f, // bottom-right
        0.0f, 1.0f, // top-left
        1.0f, 1.0f  // top-right
};

class GLEnv {

private:
    EGLDisplay display;
    EGLContext context;
    EGLSurface surface;

public:
    GLEnv();

    ~GLEnv();

    bool makeCurrent();

    bool available();
};


GLuint compileShader(GLenum type, const char *source);

GLuint linkProgram(GLuint vertexShader, GLuint fragmentShader);

GLuint genTexture(int width, int height, uint32_t *pixels, GLenum minFilter, GLenum magFilter, GLenum wrapS, GLenum wrapT);

inline bool checkFrameBufferStatus() {
    GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    return status == GL_FRAMEBUFFER_COMPLETE;
}

#endif //OPENGLEDEMO_OPENGLES_H
