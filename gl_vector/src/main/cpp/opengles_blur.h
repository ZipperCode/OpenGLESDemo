//
// Created by Zipper on 2025/7/6.
//

#ifndef OPENGLEDEMO_OPENGLES_BLUR_H
#define OPENGLEDEMO_OPENGLES_BLUR_H

#include <jni.h>
#include <GLES3/gl3.h>
#include <EGL/egl.h>
#include <android/log.h>
#include <vector>
#include <cmath>


struct GLContext {
    EGLDisplay  display;
    EGLContext  context;
    EGLSurface  surface;

    GLuint  programHorizontal;
    GLuint  programVertical;
    GLuint  originalTexture;
    GLuint  fboTexture; // 纹理用于FBO
    GLuint  framebuffer;

    // Uniform locations
    GLint   uTextureLocH;
    GLint   uImageWidthLocH;
    GLint   uBlurRadiusLocH;
    GLint   uKernelLocH;

    GLint   uTextureLocV;
    GLint   uImageHeightLocV;
    GLint   uBlurRadiusLocV;
    GLint   uKernelLocV;

    int     imageWidth;
    int     imageHeight;

    GLuint  vbo[2]; // 0 for vertices, 1 for tex coords
    GLuint  vao;    // VAO (optional for ES2, but good practice for ES3)
};

int processBlur(uint32_t *pixels, int width, int height, int radius);

#endif //OPENGLEDEMO_OPENGLES_BLUR_H
