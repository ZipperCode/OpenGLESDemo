//
// Created by Zipper on 2025/7/6.
//
#include <android/log.h>
#include <GLES3/gl3.h>
#include <EGL/egl.h>
#include <vector>
#include "image_process.h"
#include "opengles.h"

#define LOG_TAG "ImageProcess"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

const char *VERTEX_CODE = R"(
#version 300 es
layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTextureCoordinate;
out vec2 vTextureCoordinate;
void main() {
    gl_Position = aPosition;
    vTextureCoordinate = aTextureCoordinate;
}
)";

const char *BLUR_FRAGMENT_CODE = R"(
#version 300 es
precision mediump float;

uniform sampler2D uTexture;
uniform int uKernelSize;
uniform vec2 uImageSize;
uniform bool uHorizontal;

const int MAX_KERNEL_SIZE = 61;
uniform float uKernel[MAX_KERNEL_SIZE];

in vec2 vTextureCoordinate;
out vec4 fragColor;

void main() {
    vec4 sum = vec4(0.0);
    vec2 oneTexel = vec2(1.0) / uImageSize;
    float halfKernelSize = uKernelSize * 0.5;
    for (int i = 0; i < uKernelSize; i++) {
        int offsetIndex = i - halfKernelSize;
        vec2 offset = vec2(0.0);
        if (uHorizontal) {
            offset.x = float(offsetIndex) * oneTexel.x;
        } else {
            offset.y = float(offsetIndex) * oneTexel.y;
        }
        vec2 sampleCoord = clamp(vTextureCoordinate + offset, vec2(0.0), vec2(1.0));
        sum += texture(uTexture, sampleCoord) * uKernel[i];
    }
    fragColor = sum;
}
)";

const char *STORAGE_FRAGMENT_SHADER = R"(
#version 300 es
precision mediump float;
uniform sampler2D uTexture;

in vec2 vTextureCoordinate;
out vec4 fragColor;

void main() {
    vec4 color = texture(uTexture, vTextureCoordinate);
    float luminance = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    if (luminance < 0.5) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        fragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }
}
)";



// 计算高斯核
std::vector<float> genGaussianKernel(int radius, float sigma) {
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

struct GLBlurContext {
    GLEnv *env;
    GLuint program;
    // 要处理的图片纹理
    GLuint originalTexture;
    // 纹理用于FBO
    GLuint fboTexture;
    GLuint framebuffer;

    // uniform locations
    GLint uTextureLoc;
    GLint uImageSizeLoc;
    GLint uKernelSizeLoc;
    GLint uKernelLoc;
    GLint uHorizontal;

    GLuint vao; // 顶点数组对象, 绑定顶点时只需要绑定vao，ES3使用
    GLuint vbo[2]; // 0: vertices, 1: texture coordinates

    void release() {
        glDeleteTextures(1, &originalTexture);
        glDeleteTextures(1, &fboTexture);
        glDeleteFramebuffers(1, &framebuffer);
        glDeleteProgram(program);
    }
};

class GLContext {
public:
    GLuint program = -1;
    GLuint vao; // 顶点数组对象, 绑定顶点时只需要绑定vao，ES3使用
    GLuint vbo[2]; // 0: vertices, 1: texture coordinates
    // 要处理的图片纹理
    GLuint originalTexture;
    // 纹理用于FBO
    GLuint fboTexture;
    GLuint framebuffer;

    // uniform locations
    GLint uTextureLoc;

    GLContext(const char *vertexSource, const char *fragmentSource) {
        GLuint vertexShader = compileShader(GL_VERTEX_SHADER, vertexSource);
        GLuint fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentSource);
        if (vertexShader <= 0 || fragmentShader <= 0) {
            LOGE("GLContext, compileShader error");
            return;
        }
        this->program = linkProgram(vertexShader, fragmentShader);
        if (this->program <= 0) {
            return;
        }
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        glUseProgram(this->program);
        this->uTextureLoc = glGetUniformLocation(program, "uTexture");
        initVertex();
    }

    ~GLContext() {
        glDeleteProgram(this->program);
    }

    void bindOriginTexture() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this->originalTexture);
        glUniform1i(this->uTextureLoc, 0);
    }

    bool available() {
        return this->program > 0;
    }

private:
    void initVertex() {
        // 顶点数据
        glGenBuffers(2, this->vbo);
        glGenVertexArrays(1, &this->vao);

        glBindVertexArray(this->vao);

        // vertex
        glBindBuffer(GL_ARRAY_BUFFER, this->vbo[0]);
        glBufferData(GL_ARRAY_BUFFER, sizeof(VERTICES), VERTICES, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 0, (void *) 0);
        glEnableVertexAttribArray(0);

        // texture coordinate
        glBindBuffer(GL_ARRAY_BUFFER, this->vbo[1]);
        glBufferData(GL_ARRAY_BUFFER, sizeof(TEX_COORDS), TEX_COORDS, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 0, (void *) 0);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }
};

GLBlurContext *initBlurContext(GLEnv *env) {
    if (!env->makeCurrent()) {
        return nullptr;
    }
    GLBlurContext *context = new GLBlurContext();

    GLuint vertexShader = compileShader(GL_VERTEX_SHADER, VERTEX_CODE);
    GLuint fragmentShader = compileShader(GL_FRAGMENT_SHADER, BLUR_FRAGMENT_CODE);
    if (vertexShader <= 0 || fragmentShader <= 0) {
        LOGE("initBlurContext, compileShader error");
        delete context;
        return nullptr;
    }
    GLuint program = linkProgram(vertexShader, fragmentShader);
    if (program <= 0) {
        LOGE("initBlurContext, linkProgram error");
        delete context;
        return nullptr;
    }
    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);
    glUseProgram(program);
    context->uTextureLoc = glGetUniformLocation(program, "uTexture");
    context->uImageSizeLoc = glGetUniformLocation(program, "uImageSize");
    context->uKernelSizeLoc = glGetUniformLocation(program, "uKernelSize");
    context->uKernelLoc = glGetUniformLocation(program, "uKernel");

    // 顶点数据
    glGenBuffers(2, context->vbo);
    glGenVertexArrays(1, &context->vao);

    glBindVertexArray(context->vao);

    // vertex
    glBindBuffer(GL_ARRAY_BUFFER, context->vbo[0]);
    glBufferData(GL_ARRAY_BUFFER, sizeof(VERTICES), VERTICES, GL_STATIC_DRAW);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 0, (void *) 0);
    glEnableVertexAttribArray(0);

    // texture coordinate
    glBindBuffer(GL_ARRAY_BUFFER, context->vbo[1]);
    glBufferData(GL_ARRAY_BUFFER, sizeof(TEX_COORDS), TEX_COORDS, GL_STATIC_DRAW);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 0, (void *) 0);
    glEnableVertexAttribArray(1);

    glBindVertexArray(0);
    LOGD("initBlurContext 成功 是否存在错误 = %x", eglGetError());
    return context;
}

int imageBlur(GLEnv *env, uint32_t *pixels, int width, int height, int radius) {
    if (pixels == nullptr || width <= 0 || height <= 0) {
        LOGE("imageBlur Invalid parameters.");
        return -1;
    }
    LOGD("imageBlur width = %d, height = %d, radius = %d", width, height, radius);
    GLBlurContext *context = initBlurContext(env);
    if (context == nullptr) {
        return -1;
    }
    float sigma = static_cast<float>(radius) / 3.0f;
    std::vector<float> kernel = genGaussianKernel(radius, sigma);
    LOGD("Kernel size: %d", static_cast<int>(kernel.size()));
    // 原图纹理
    context->originalTexture = genTexture(width, height, pixels, GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
    // 帧缓冲
    glGenFramebuffers(1, &context->framebuffer);
    context->fboTexture = genTexture(width, height, nullptr, GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
    // 临时缓冲纹理
    GLuint tempTexture = genTexture(width, height, nullptr, GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);

    // 临时纹理绑定帧缓冲
    glViewport(0, 0, width, height);
    glBindVertexArray(context->vao);
    glBindFramebuffer(GL_FRAMEBUFFER, context->framebuffer);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tempTexture, 0);
    if (checkFrameBufferStatus()) {
        LOGE("imageBlur tempTexture bind Framebuffer is not complete!");
        glDeleteTextures(1, &tempTexture);
        context->release();
        delete context;
        return -1;
    }

    GLfloat *size = new GLfloat[2]{width * 1.0f, height * 1.0f};

    // 水平高斯模糊
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    glUseProgram(context->program);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, context->originalTexture);
    glUniform1i(context->uTextureLoc, 0);
    glUniform1i(context->uKernelSizeLoc, static_cast<int>(radius / 2));
    glUniform1fv(context->uImageSizeLoc, 2, size);
    glUniform1fv(context->uKernelLoc, kernel.size(), kernel.data());
    glUniform1i(context->uHorizontal, 1);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glBindFramebuffer(GL_FRAMEBUFFER, context->framebuffer);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, context->fboTexture, 0);
    if (!checkFrameBufferStatus()) {
        LOGE("imageBlur fboTexture bind Framebuffer is not complete!");
        glDeleteTextures(1, &tempTexture);
        context->release();
        delete context;
        return -1;
    }

    // 垂直
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    glUseProgram(context->program);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, context->originalTexture);
    glUniform1i(context->uTextureLoc, 0);
    glUniform1i(context->uKernelSizeLoc, static_cast<int>(radius / 2));
    glUniform1fv(context->uImageSizeLoc, 2, size);
    glUniform1fv(context->uKernelLoc, kernel.size(), kernel.data());
    glUniform1i(context->uHorizontal, 0);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    // 读取像素
    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    delete context;
    return 0;
}

class StorageLineArtContext : public GLContext {

public:

    StorageLineArtContext(const char *vertexSource, const char *fragmentSource)
            : GLContext(vertexSource, fragmentSource) {
    }
};

/**
 * 像素加粗
 * @param env EGL环境
 * @param pixels 像素
 * @param width 宽度
 * @param height 高度
 * @return
 */
int storageLineArt(GLEnv *env, uint32_t *pixels, int width, int height) {
    if (!env || !env->makeCurrent()) {
        return -1;
    }
    StorageLineArtContext *context = new StorageLineArtContext(VERTEX_CODE, STORAGE_FRAGMENT_SHADER);
    if (!context->available()) {
        delete context;
        return -1;
    }
    context->originalTexture = genTexture(width, height, pixels, GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
    // 帧缓冲
    glGenFramebuffers(1, &context->framebuffer);
    context->fboTexture = genTexture(width, height, nullptr, GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);

    glViewport(0, 0, width, height);
    glBindVertexArray(context->vao);

    glBindFramebuffer(GL_FRAMEBUFFER, context->framebuffer);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, context->fboTexture, 0);
    if (checkFrameBufferStatus()) {
        LOGE("storageLineArt tempTexture bind Framebuffer is not complete!");
        glDeleteTextures(1, &context->fboTexture);
        delete context;
        return -1;
    }
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    glUseProgram(context->program);
    context->bindOriginTexture();
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    // 读取像素
    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    delete context;
    return 0;
}