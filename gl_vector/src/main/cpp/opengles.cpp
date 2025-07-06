//
// Created by Zipper on 2025/7/6.
//
#include <jni.h>
#include "opengles.h"
#include <vector>
#include <android/log.h>

#define LOG_TAG "GLEnv"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

GLEnv::GLEnv() {
    this->display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (this->display == EGL_NO_DISPLAY) {
        LOGE("eglGetDisplay failed");
        return;
    }
    EGLint major, minor;
    if (!eglInitialize(this->display, &major, &minor)) {
        LOGE("eglInitialize failed");
        eglTerminate(this->display);
        this->display = EGL_NO_DISPLAY;
        return;
    }
    EGLConfig config;
    EGLint numConfigs;
    EGLint attribs[] = {
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
            EGL_SURFACE_TYPE, EGL_PBUFFER_BIT, // Off-screen rendering
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_NONE
    };
    if (!eglChooseConfig(this->display, attribs, &config, 1, &numConfigs)) {
        LOGE("eglChooseConfig failed");
        eglTerminate(this->display);
        this->display = EGL_NO_DISPLAY;
        return;
    }

    EGLint contextAttribs[] = {
            EGL_CONTEXT_CLIENT_VERSION, 3, // OpenGL ES 3.0
            EGL_NONE
    };

    this->context = eglCreateContext(this->display, config, EGL_NO_CONTEXT, contextAttribs);
    if (this->context == EGL_NO_CONTEXT) {
        LOGE("eglCreateContext failed: %x", eglGetError());
        eglTerminate(this->display);
        this->display = EGL_NO_DISPLAY;
        return;
    }

    // 创建离屏幕渲染缓冲区
    EGLint pbufferAttribs[] = {
            EGL_WIDTH, 1,
            EGL_HEIGHT, 1,
            EGL_NONE
    };
    this->surface = eglCreatePbufferSurface(this->display, config, pbufferAttribs);
    if (this->surface == EGL_NO_SURFACE) {
        LOGE("eglCreatePbufferSurface failed: %x", eglGetError());
        eglDestroyContext(this->display, this->context);
        eglTerminate(this->display);
        this->display = EGL_NO_DISPLAY;
        this->context = EGL_NO_CONTEXT;
        return;
    }
}

GLEnv::~GLEnv() {
    eglDestroySurface(this->display, this->surface);
    eglDestroyContext(this->display, this->context);
    eglTerminate(this->display);
    this->display = EGL_NO_DISPLAY;
    this->context = EGL_NO_CONTEXT;
    this->surface = EGL_NO_SURFACE;
}

bool GLEnv::makeCurrent() {
    if (!available()) {
        return false;
    }
    if (!eglMakeCurrent(this->display, this->surface, this->surface, this->context)) {
        LOGE("eglMakeCurrent failed: %x", eglGetError());
        eglDestroySurface(this->display, this->surface);
        eglDestroyContext(this->display, this->context);
        eglTerminate(this->display);
        this->display = EGL_NO_DISPLAY;
        this->context = EGL_NO_CONTEXT;
        this->surface = EGL_NO_SURFACE;
        return false;
    }

    return true;
}

bool GLEnv::available() {
    return this->context != nullptr && this->display != nullptr && this->surface != nullptr;
}

// 编译着色器
GLuint compileShader(GLenum type, const char *source) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, NULL);
    glCompileShader(shader);

    GLint status;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &status);
    if (status == GL_FALSE) {
        GLint length;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &length);
        std::vector<char> log(length);
        glGetShaderInfoLog(shader, length, &length, &log[0]);
        LOGE("Shader compilation failed: %s", log.data());
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

// 链接程序
GLuint linkProgram(GLuint vertexShader, GLuint fragmentShader) {
    GLuint program = glCreateProgram();
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glLinkProgram(program);

    GLint status;
    glGetProgramiv(program, GL_LINK_STATUS, &status);
    if (status == GL_FALSE) {
        GLint length;
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, &length);
        std::vector<char> log(length);
        glGetProgramInfoLog(program, length, &length, &log[0]);
        LOGE("Program linking failed: %s", log.data());
        glDeleteProgram(program);
        return 0;
    }
    return program;
}


GLuint genTexture(int width, int height, uint32_t *pixels, GLenum minFilter, GLenum magFilter, GLenum wrapS, GLenum wrapT) {
    GLuint texture;
    glGenTextures(1, &texture);
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
    glBindTexture(GL_TEXTURE_2D, 0);
    return texture;
}



