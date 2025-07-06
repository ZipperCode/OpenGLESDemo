//
// Created by Zipper on 2025/7/6.
//
#include "opengles_blur.h"
#include "opengles.h"
#define LOG_TAG_OPENGL "OpenGLESBlur"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_OPENGL, __VA_ARGS__)
#ifndef LOGE
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG_OPENGL, __VA_ARGS__)
#endif
// 着色器代码
const char *VERTEX_SHADER_CODE =
        "#version 300 es\n"
        "layout(location = 0) in vec4 aPosition;\n"
        "layout(location = 1) in vec2 aTexCoord;\n"
        "out vec2 vTexCoord;\n"
        "void main() {\n"
        "    gl_Position = aPosition;\n"
        "    vTexCoord = aTexCoord;\n"
        "}\n";

const char *FRAGMENT_SHADER_GAUSSIAN_HORIZONTAL_CODE =
        "#version 300 es\n"
        "precision mediump float;\n"
        "in vec2 vTexCoord;\n"
        "out vec4 fragColor;\n"
        "uniform sampler2D uTexture;\n"
        "uniform float uImageWidth;\n"
        "uniform float uBlurRadius;\n"
        "const int MAX_KERNEL_SIZE = 61; // Should be odd\n"
        "uniform float uKernel[MAX_KERNEL_SIZE];\n"
        "void main() {\n"
        "    float texelSizeX = 1.0 / uImageWidth;\n"
        "    vec4 sum = vec4(0.0);\n"
        "    int radiusInt = int(uBlurRadius);\n"
        "    for (int i = 0; i < MAX_KERNEL_SIZE; i++) {\n"
        "        if (i >= 2 * radiusInt + 1) break;\n"
        "        int offsetIndex = i - radiusInt;\n"
        "        sum += texture(uTexture, vTexCoord + vec2(float(offsetIndex) * texelSizeX, 0.0)) * uKernel[i];\n"
        "    }\n"
        "    fragColor = sum;\n"
        "}\n";

const char *FRAGMENT_SHADER_GAUSSIAN_VERTICAL_CODE =
        "#version 300 es\n"
        "precision mediump float;\n"
        "in vec2 vTexCoord;\n"
        "out vec4 fragColor;\n"
        "uniform sampler2D uTexture;\n"
        "uniform float uImageHeight;\n"
        "uniform float uBlurRadius;\n"
        "const int MAX_KERNEL_SIZE = 61; // Should be odd\n"
        "uniform float uKernel[MAX_KERNEL_SIZE];\n"
        "void main() {\n"
        "    float texelSizeY = 1.0 / uImageHeight;\n"
        "    vec4 sum = vec4(0.0);\n"
        "    int radiusInt = int(uBlurRadius);\n"
        "    for (int i = 0; i < MAX_KERNEL_SIZE; i++) {\n"
        "        if (i >= 2 * radiusInt + 1) break;\n"
        "        int offsetIndex = i - radiusInt;\n"
        "        sum += texture(uTexture, vTexCoord + vec2(0.0, float(offsetIndex) * texelSizeY)) * uKernel[i];\n"
        "    }\n"
        "    fragColor = sum;\n"
        "}\n";

// 计算高斯核
std::vector<float> calculateGaussianKernel(int radius, float sigma) {
    int kernelSize = 2 * radius + 1;
    std::vector<float> kernel(kernelSize);
    float sum = 0.0f;

    for (int i = 0; i < kernelSize; ++i) {
        float x = (float) i - radius;
        kernel[i] = expf(-(x * x) / (2.0f * sigma * sigma));
        sum += kernel[i];
    }

    // 归一化
    for (int i = 0; i < kernelSize; ++i) {
        kernel[i] /= sum;
    }
    return kernel;
}

GLContext *initGl() {
    GLContext *glContext = new GLContext();
    memset(glContext, 0, sizeof(GLContext));

    glContext->display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (glContext->display == EGL_NO_DISPLAY) {
        LOGE("eglGetDisplay failed");
        delete glContext;
        return nullptr;
    }

    EGLint major, minor;
    if (!eglInitialize(glContext->display, &major, &minor)) {
        LOGE("eglInitialize failed");
        eglTerminate(glContext->display);
        delete glContext;
        return 0;
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

    if (!eglChooseConfig(glContext->display, attribs, &config, 1, &numConfigs)) {
        LOGE("eglChooseConfig failed");
        eglTerminate(glContext->display);
        delete glContext;
        return 0;
    }

    EGLint contextAttribs[] = {
            EGL_CONTEXT_CLIENT_VERSION, 3, // OpenGL ES 3.0
            EGL_NONE
    };

    glContext->context = eglCreateContext(glContext->display, config, EGL_NO_CONTEXT, contextAttribs);
    if (glContext->context == EGL_NO_CONTEXT) {
        LOGE("eglCreateContext failed: %x", eglGetError());
        eglTerminate(glContext->display);
        delete glContext;
        return 0;
    }

    // Create a dummy PBUFFER surface for off-screen rendering
    EGLint pbufferAttribs[] = {
            EGL_WIDTH, 1,
            EGL_HEIGHT, 1,
            EGL_NONE
    };
    glContext->surface = eglCreatePbufferSurface(glContext->display, config, pbufferAttribs);
    if (glContext->surface == EGL_NO_SURFACE) {
        LOGE("eglCreatePbufferSurface failed: %x", eglGetError());
        eglDestroyContext(glContext->display, glContext->context);
        eglTerminate(glContext->display);
        delete glContext;
        return 0;
    }

    if (!eglMakeCurrent(glContext->display, glContext->surface, glContext->surface, glContext->context)) {
        LOGE("eglMakeCurrent failed: %x", eglGetError());
        eglDestroySurface(glContext->display, glContext->surface);
        eglDestroyContext(glContext->display, glContext->context);
        eglTerminate(glContext->display);
        delete glContext;
        return 0;
    }

    // Compile shaders and link programs
    GLuint vertexShader = compileShader(GL_VERTEX_SHADER, VERTEX_SHADER_CODE);
    GLuint fragmentShaderH = compileShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER_GAUSSIAN_HORIZONTAL_CODE);
    GLuint fragmentShaderV = compileShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER_GAUSSIAN_VERTICAL_CODE);

    if (vertexShader == 0 || fragmentShaderH == 0 || fragmentShaderV == 0) {
        LOGE("Shader compilation failed during init");
        eglMakeCurrent(glContext->display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroySurface(glContext->display, glContext->surface);
        eglDestroyContext(glContext->display, glContext->context);
        eglTerminate(glContext->display);
        delete glContext;
        return 0;
    }

    glContext->programHorizontal = linkProgram(vertexShader, fragmentShaderH);
    glContext->programVertical = linkProgram(vertexShader, fragmentShaderV);

    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShaderH);
    glDeleteShader(fragmentShaderV);

    if (glContext->programHorizontal == 0 || glContext->programVertical == 0) {
        LOGE("Program linking failed during init");
        eglMakeCurrent(glContext->display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroySurface(glContext->display, glContext->surface);
        eglDestroyContext(glContext->display, glContext->context);
        eglTerminate(glContext->display);
        delete glContext;
        return 0;
    }

    // Get uniform locations for horizontal blur
    glUseProgram(glContext->programHorizontal);
    glContext->uTextureLocH = glGetUniformLocation(glContext->programHorizontal, "uTexture");
    glContext->uImageWidthLocH = glGetUniformLocation(glContext->programHorizontal, "uImageWidth");
    glContext->uBlurRadiusLocH = glGetUniformLocation(glContext->programHorizontal, "uBlurRadius");
    glContext->uKernelLocH = glGetUniformLocation(glContext->programHorizontal, "uKernel");

    // Get uniform locations for vertical blur
    glUseProgram(glContext->programVertical);
    glContext->uTextureLocV = glGetUniformLocation(glContext->programVertical, "uTexture");
    glContext->uImageHeightLocV = glGetUniformLocation(glContext->programVertical, "uImageHeight");
    glContext->uBlurRadiusLocV = glGetUniformLocation(glContext->programVertical, "uBlurRadius");
    glContext->uKernelLocV = glGetUniformLocation(glContext->programVertical, "uKernel");

    // Generate VBOs and VAO
    glGenBuffers(2, glContext->vbo);
    glGenVertexArrays(1, &glContext->vao);

    glBindVertexArray(glContext->vao);

    // Vertex data
    glBindBuffer(GL_ARRAY_BUFFER, glContext->vbo[0]);
    glBufferData(GL_ARRAY_BUFFER, sizeof(VERTICES), VERTICES, GL_STATIC_DRAW);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 0, (void *) 0);
    glEnableVertexAttribArray(0);

    // Texture coordinate data
    glBindBuffer(GL_ARRAY_BUFFER, glContext->vbo[1]);
    glBufferData(GL_ARRAY_BUFFER, sizeof(TEX_COORDS), TEX_COORDS, GL_STATIC_DRAW);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 0, (void *) 0);
    glEnableVertexAttribArray(1);

    glBindVertexArray(0); // Unbind VAO

    LOGI("OpenGL ES context initialized successfully.");
    return glContext;
}

int processBlur(uint32_t *pixels, int width, int height, int radius) {
    if (pixels == nullptr || width <= 0 || height <= 0) {
        LOGE("Invalid parameters.");
        return -1;
    }
    GLContext *glContext = initGl();
    if (glContext == nullptr) {
        return -1;
    }

    if (!eglMakeCurrent(glContext->display, glContext->surface, glContext->surface, glContext->context)) {
        LOGE("eglMakeCurrent failed.");
        delete glContext;
        return -1;
    }

    float sigma = static_cast<float>(radius) / 3.0f;
    std::vector<float> kernel = calculateGaussianKernel(radius, sigma);
    LOGI("Kernel size: %d", static_cast<int>(kernel.size()));

    glContext->imageWidth = width;
    glContext->imageHeight = height;
    // 原图
    glGenTextures(1, &glContext->originalTexture);
    glBindTexture(GL_TEXTURE_2D, glContext->originalTexture);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
    glBindTexture(GL_TEXTURE_2D, 0);

    glGenFramebuffers(1, &glContext->framebuffer);
    LOGI("Texture loaded: width=%d, height=%d", width, height);

    // 缓冲区
    glGenTextures(1, &glContext->fboTexture);
    glBindTexture(GL_TEXTURE_2D, glContext->fboTexture);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
    glBindTexture(GL_TEXTURE_2D, 0);

    // 中间缓冲区
    GLuint tempTexture;
    glGenTextures(1, &tempTexture);
    glBindTexture(GL_TEXTURE_2D, tempTexture);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
    glBindTexture(GL_TEXTURE_2D, 0);

    // 绘制水平高斯模糊到临时纹理
    glViewport(0, 0, width, height);
    glBindVertexArray(glContext->vao);
    glBindFramebuffer(GL_FRAMEBUFFER, glContext->framebuffer);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tempTexture, 0);
    GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if (status != GL_FRAMEBUFFER_COMPLETE) {
        LOGE("Framebuffer is not complete: %d", status);
        delete glContext;
        return -1;
    }

    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    // 水平高斯模糊
    glUseProgram(glContext->programHorizontal);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, glContext->originalTexture);
    glUniform1i(glContext->uTextureLocH, 0);
    glUniform1f(glContext->uImageWidthLocH, static_cast<float>(glContext->imageWidth));
    glUniform1f(glContext->uBlurRadiusLocH, static_cast<float>(radius));
    glUniform1fv(glContext->uKernelLocH, kernel.size(), kernel.data());
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    // 最终缓冲区
    glBindFramebuffer(GL_FRAMEBUFFER, glContext->framebuffer);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, glContext->fboTexture, 0);
    status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if (status != GL_FRAMEBUFFER_COMPLETE) {
        LOGE("Framebuffer is not complete: %d", status);
        return -1;
    }
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    glUseProgram(glContext->programHorizontal);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, tempTexture);
    glUniform1i(glContext->uTextureLocV, 0);
    glUniform1f(glContext->uImageHeightLocV, static_cast<float>(glContext->imageHeight));
    glUniform1f(glContext->uBlurRadiusLocV, static_cast<float>(radius));
    glUniform1fv(glContext->uKernelLocV, kernel.size(), kernel.data());
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glBindFramebuffer(GL_FRAMEBUFFER, glContext->framebuffer);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, glContext->fboTexture, 0);
    status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if (status != GL_FRAMEBUFFER_COMPLETE) {
        LOGE("Framebuffer is not complete: %d", status);
        delete glContext;
        return -1;
    }

    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    LOGI("Pixels read successfully.");

    glDeleteTextures(1, &tempTexture);
    glDeleteTextures(1, &glContext->fboTexture);
    glDeleteFramebuffers(1, &glContext->framebuffer);
    glDeleteProgram(glContext->programVertical);
    glDeleteProgram(glContext->programHorizontal);
    glDeleteBuffers(2, glContext->vbo);
    glDeleteVertexArrays(1, &glContext->vao);
    eglDestroySurface(glContext->display, glContext->surface);
    eglDestroyContext(glContext->display, glContext->context);
    eglTerminate(glContext->display);

    delete glContext;
    return 0;
}

