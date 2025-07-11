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
uniform float uBlurRadius;
uniform vec2 uImageSize;
uniform bool uHorizontal;

const int MAX_KERNEL_SIZE = 61;
uniform float uKernel[MAX_KERNEL_SIZE];

in vec2 vTextureCoordinate;
out vec4 fragColor;

void main() {
    vec4 sum = vec4(0.0);
    float sumGray = 0.0;
    vec2 oneTexel = vec2(1.0) / uImageSize;
    int radiusInt = int(uBlurRadius);
    for (int i = 0; i < MAX_KERNEL_SIZE; i++) {
        if (i >= 2 * radiusInt + 1) break;
        int offsetIndex = i - radiusInt;
        vec2 offset = vec2(0.0);
        if (uHorizontal) {
            offset.x = float(offsetIndex) * oneTexel.x;
        } else {
            offset.y = float(offsetIndex) * oneTexel.y;
        }
        vec2 sampleCoord = clamp(vTextureCoordinate + offset, vec2(0.0), vec2(1.0));
        float gray = dot(texture(uTexture, sampleCoord).rgb, vec3(0.2126, 0.7152, 0.0722));
        sumGray += gray * uKernel[i];
    }
    fragColor = vec4(sumGray, sumGray, sumGray, 1.0);
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
    if (luminance < 0.9) {
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

class GLContext {
public:
    GLuint program = -1;
    GLuint vao{}; // 顶点数组对象, 绑定顶点时只需要绑定vao，ES3使用
    GLuint vbo[2]{}; // 0: vertices, 1: texture coordinates
    // 要处理的图片纹理
    GLuint originalTexture{};
    // 纹理用于FBO
    GLuint fboTexture{};
    GLuint framebuffer{};

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
        // 帧缓冲
        glGenFramebuffers(1, &framebuffer);
    }

    ~GLContext() {
        glDeleteProgram(this->program);
    }

    void bindOriginTexture() const {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this->originalTexture);
        glUniform1i(this->uTextureLoc, 0);
    }

    bool available() const {
        return this->program > 0;
    }

private:
    void initVertex() {
        // 顶点数据
        glGenBuffers(2, this->vbo);
        glGenVertexArrays(1, &this->vao);

        glBindVertexArray(this->vao);

        // Vertex data
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBufferData(GL_ARRAY_BUFFER, sizeof(VERTICES), VERTICES, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 0, (void *) 0);
        glEnableVertexAttribArray(0);

        // Texture coordinate data
        glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        glBufferData(GL_ARRAY_BUFFER, sizeof(TEX_COORDS), TEX_COORDS, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 0, (void *) 0);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }
};

class GLBlurContext : public GLContext {
public:
    // uniform locations
    GLint uImageSizeLoc{};
    GLint uBlurRadiusLoc{};
    GLint uKernelLoc{};
    GLint uHorizontalLoc{};

    GLBlurContext() : GLContext(VERTEX_CODE, BLUR_FRAGMENT_CODE) {
        uImageSizeLoc = glGetUniformLocation(program, "uImageSize");
        uBlurRadiusLoc = glGetUniformLocation(program, "uBlurRadius");
        uKernelLoc = glGetUniformLocation(program, "uKernel");
        uHorizontalLoc = glGetUniformLocation(program, "uHorizontal");
    }

    ~GLBlurContext() {
        glDeleteTextures(1, &originalTexture);
        glDeleteTextures(1, &fboTexture);
        glDeleteFramebuffers(1, &framebuffer);
        glDeleteProgram(program);
    }

};

int imageBlur(GLEnv *env, uint32_t *pixels, int width, int height, int radius) {
    if (pixels == nullptr || width <= 0 || height <= 0) {
        LOGE("imageBlur Invalid parameters.");
        return -1;
    }
    LOGD("imageBlur width = %d, height = %d, radius = %d", width, height, radius);
    if (!env || !env->makeCurrent()) {
        LOGE("imageBlur makeCurrent error.");
        return -1;
    }
    auto *context = new GLBlurContext();
    float sigma = static_cast<float>(radius) / 3.0f;
    std::vector<float> kernel = genGaussianKernel(radius, sigma);
    LOGD("Kernel size: %d", static_cast<int>(kernel.size()));
    // 原图纹理
    context->originalTexture = genTexture(width, height, pixels, GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
    context->fboTexture = genTexture(width, height, nullptr, GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
    // 临时缓冲纹理
    GLuint tempTexture = genTexture(width, height, nullptr, GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);

    // 临时纹理绑定帧缓冲
    glViewport(0, 0, width, height);
    glBindVertexArray(context->vao);
    glBindFramebuffer(GL_FRAMEBUFFER, context->framebuffer);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tempTexture, 0);
    if (!checkFrameBufferStatus()) {
        LOGE("imageBlur tempTexture bind Framebuffer is not complete!");
        glDeleteTextures(1, &tempTexture);
        delete context;
        return -1;
    }
    LOGD("开始水平高斯模糊 = %x", glGetError());
    GLfloat size[2] = {static_cast<GLfloat>(width), static_cast<GLfloat>(height)};

    // 水平高斯模糊
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    glUseProgram(context->program);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, context->originalTexture);
    glUniform1i(context->uTextureLoc, 0);
    glUniform1f(context->uBlurRadiusLoc, static_cast<float >(radius));
    glUniform2fv(context->uImageSizeLoc, 1, size);
    glUniform1fv(context->uKernelLoc, static_cast<GLsizei>(kernel.size()), kernel.data());
    glUniform1i(context->uHorizontalLoc, 1);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    // 最终缓冲区
    glBindFramebuffer(GL_FRAMEBUFFER, context->framebuffer);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, context->fboTexture, 0);
    if (!checkFrameBufferStatus()) {
        LOGE("imageBlur fboTexture bind Framebuffer is not complete!");
        glDeleteTextures(1, &tempTexture);
        delete context;
        return -1;
    }
    LOGD("开始垂直高斯模糊 = %x", glGetError());
    // 垂直
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    //glUseProgram(context->program);
//    glActiveTexture(GL_TEXTURE0);
//    glBindTexture(GL_TEXTURE_2D, context->originalTexture);
//    glUniform1i(context->uTextureLoc, 0);
//    glUniform1f(context->uBlurRadiusLoc, static_cast<float >(radius));
//    glUniform1fv(context->uImageSizeLoc, 1, size);
//    glUniform1fv(context->uKernelLoc, static_cast<GLsizei>(kernel.size()), kernel.data());
    glUniform1i(context->uHorizontalLoc, 0);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    glUseProgram(0);
    LOGD("开始读取像素 = %x", glGetError());
    // 读取像素
    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    LOGD("imageBlur finish hasError = %x", glGetError());

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
        LOGE("storageLineArt, makeCurrent error");
        return -1;
    }
    auto *context = new StorageLineArtContext(VERTEX_CODE, STORAGE_FRAGMENT_SHADER);
    if (!context->available()) {
        LOGE("StorageLineArtContext init failed.");
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
    if (!checkFrameBufferStatus()) {
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
    LOGD("storageLineArt finish isError = %x", glGetError());
    delete context;
    return 0;
}