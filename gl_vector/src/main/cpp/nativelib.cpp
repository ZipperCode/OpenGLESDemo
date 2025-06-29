#pragma once

#include <jni.h>
#include <jni.h>
#include <string>
#include <queue>
#include <ctime>
#include <cstdint>
#include <random>
#include <cmath>
#include <unordered_map>

#include <android/log.h>
#include <android/bitmap.h> // 用于操作 Android Bitmap

// 定义日志宏
#define LOG_TAG "NativeRegionCalculator"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
struct Point {
    int x, y;
};


const int lineColorThreshold = 10;
const float minColorDistance = 60.f;
// 最小区域的面积
const int minRegionAreaThreshold = 10;
const int threashold = 10;

// 计算RGB颜色之间的欧几里得距离
float calculateColorDistance(uint32_t color1, uint32_t color2) {
    uint8_t r1 = (color1 >> 16) & 0xFF;
    uint8_t g1 = (color1 >> 8) & 0xFF;
    uint8_t b1 = color1 & 0xFF;

    uint8_t r2 = (color2 >> 16) & 0xFF;
    uint8_t g2 = (color2 >> 8) & 0xFF;
    uint8_t b2 = color2 & 0xFF;

    auto dr = static_cast<float>(r1 - r2);
    auto dg = static_cast<float>(g1 - g2);
    auto db = static_cast<float>(b1 - b2);

    return std::sqrt(dr * dr + dg * dg + db * db);
}
// 计算两个颜色的欧几里得距离
float colorDistance(uint32_t c1, uint32_t c2) {
    int r1 = (c1 >> 16) & 0xFF;
    int g1 = (c1 >> 8) & 0xFF;
    int b1 = c1 & 0xFF;
    int r2 = (c2 >> 16) & 0xFF;
    int g2 = (c2 >> 8) & 0xFF;
    int b2 = c2 & 0xFF;
    return std::sqrt((r1-r2)*(r1-r2) + (g1-g2)*(g1-g2) + (b1-b2)*(b1-b2));
}

// HSL转RGB
uint32_t hslToRgb(float h, float s, float l) {
    float c = (1 - std::fabs(2 * l - 1)) * s;
    float x = c * (1 - std::fabs(std::fmod(h / 60.0, 2) - 1));
    float m = l - c/2;

    float r, g, b;
    if (h >= 0 && h < 60) {
        r = c; g = x; b = 0;
    } else if (h >= 60 && h < 120) {
        r = x; g = c; b = 0;
    } else if (h >= 120 && h < 180) {
        r = 0; g = c; b = x;
    } else if (h >= 180 && h < 240) {
        r = 0; g = x; b = c;
    } else if (h >= 240 && h < 300) {
        r = x; g = 0; b = c;
    } else {
        r = c; g = 0; b = x;
    }

    int ri = static_cast<int>((r + m) * 255);
    int gi = static_cast<int>((g + m) * 255);
    int bi = static_cast<int>((b + m) * 255);

    return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
}

// 分层颜色生成器
class ColorGenerator {
private:
    // 基础颜色表
    std::vector<uint32_t> baseColors;
    // 已生成颜色缓存
    std::unordered_map<int, uint32_t> colorCache;
    // 区域大小
    const int regionSize = 100;
    // 黄金比例常数
    const float goldenRatio = 0.618033988749895f;

public:
    ColorGenerator() {
        // 初始化基础颜色表
        baseColors = {
                0xFF000000, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF,
                0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF, 0xFF800000,
                0xFF008000, 0xFF000080, 0xFF808000, 0xFF800080,
                0xFF008080, 0xFFC0C0C0, 0xFF808080, 0xFF404040
        };
    }

    // 为特定index生成唯一颜色
    uint32_t getColor(int index) {
        // 检查缓存
        auto it = colorCache.find(index);
        if (it != colorCache.end()) {
            return it->second;
        }

        // 计算区域和区域内索引
        int region = index / regionSize;
        int offset = index % regionSize;

        // 基础颜色
        uint32_t baseColor = baseColors[region % baseColors.size()];

        // 在基础颜色上进行微调
        float hue = (offset * goldenRatio) * 360.0f;
        float saturation = 0.7f + (offset % 3) * 0.1f;
        float lightness = 0.6f + ((offset / 3) % 3) * 0.1f;

        uint32_t generatedColor = hslToRgb(hue, saturation, lightness);

        // 确保与同区域的基础颜色距离足够大
        if (colorDistance(generatedColor, baseColor) < 60) {
            // 微调亮度
            lightness = (lightness + 0.3f) > 1.0f ? lightness - 0.3f : lightness + 0.3f;
            generatedColor = hslToRgb(hue, saturation, lightness);
        }

        // 缓存结果
        colorCache[index] = generatedColor;
        return generatedColor;
    }
};

// 全局生成器实例
static ColorGenerator colorGenerator;

// 3D空间点哈希函数
uint32_t hashPoint(int x, int y, int z) {
    uint32_t hash = 2166136261u;
    hash ^= x;
    hash *= 16777619;
    hash ^= y;
    hash *= 16777619;
    hash ^= z;
    hash *= 16777619;
    return hash;
}

// 从哈希值生成颜色
uint32_t hashToColor(uint32_t hash) {
    int r = (hash >> 16) & 0xFF;
    int g = (hash >> 8) & 0xFF;
    int b = hash & 0xFF;
    return 0xFF000000 | (r << 16) | (g << 8) | (b << 0);
}

// 多维空间分区颜色生成器
class ColorGenerator2 {
private:
    // 区域大小 (确保立方体对角线长度 < 60)
    const int regionSize = 34; // 34*sqrt(3) ≈ 58.9 < 60
    // 区域偏移量，确保区域间的最小距离 > 60
    const int regionOffset = 61;
    // 随机旋转矩阵参数
    const float rotationMatrix[3][3] = {
            {0.70710678, -0.5, 0.5},
            {0.5, 0.70710678, 0.5},
            {-0.5, -0.5, 0.70710678}
    };
    // 缓存已生成的颜色
    std::unordered_map<int, uint32_t> colorCache;

    // 3D空间坐标转换
    void transformCoordinates(int index, int& x, int& y, int& z) {
        // 使用质数确保均匀分布
        const int p1 = 73856093;
        const int p2 = 19349663;
        const int p3 = 83492791;

        // 3D康威函数，将1D映射到3D
        x = (index * p1) ^ ((index >> 16) * p2);
        y = (index * p2) ^ ((index >> 12) * p3);
        z = (index * p3) ^ ((index >> 8) * p1);

        // 应用旋转矩阵，增加随机性
        int tx = x;
        int ty = y;
        int tz = z;
        x = static_cast<int>(tx * rotationMatrix[0][0] + ty * rotationMatrix[0][1] + tz * rotationMatrix[0][2]);
        y = static_cast<int>(tx * rotationMatrix[1][0] + ty * rotationMatrix[1][1] + tz * rotationMatrix[1][2]);
        z = static_cast<int>(tx * rotationMatrix[2][0] + ty * rotationMatrix[2][1] + tz * rotationMatrix[2][2]);
    }

public:
    // 为特定index生成唯一颜色
    uint32_t getColor(int index) {
        // 检查缓存
        auto it = colorCache.find(index);
        if (it != colorCache.end()) {
            return it->second;
        }

        // 计算3D空间坐标
        int x, y, z;
        transformCoordinates(index, x, y, z);

        // 计算区域中心坐标
        int regionX = x * regionOffset;
        int regionY = y * regionOffset;
        int regionZ = z * regionOffset;

        // 确保坐标在有效范围内 (0-255)
        regionX = std::abs(regionX) % (256 - regionSize);
        regionY = std::abs(regionY) % (256 - regionSize);
        regionZ = std::abs(regionZ) % (256 - regionSize);

        // 在区域内生成随机颜色
        uint32_t hash = hashPoint(regionX, regionY, regionZ);
        uint32_t baseColor = hashToColor(hash);

        // 微调颜色，确保每个区域内的颜色也有差异
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;

        // 使用index的低位进行微调
        r = (r + (index * 17) % regionSize) % 256;
        g = (g + (index * 23) % regionSize) % 256;
        b = (b + (index * 29) % regionSize) % 256;

        uint32_t finalColor = 0xFF000000 | (r << 16) | (g << 8) | b;

        // 缓存结果
        colorCache[index] = finalColor;
        return finalColor;
    }
};

// 全局生成器实例
static ColorGenerator2 colorGenerator2;

// 判断像素是否为线条颜色 (C++版本)
// 假设线条是黑色或深色，R, G, B 值都低于某个阈值
bool isLineColor(uint32_t pixel, int threshold) {
    uint8_t a = (pixel >> 24) & 0xFF;
    if (a == 0) {
        return false;
    }
    int r = (pixel >> 16) & 0xFF; // ARGB_8888
    int g = (pixel >> 8) & 0xFF;
    int b = pixel & 0xFF;
    return r < threshold && g < threshold && b < threshold;
}


extern "C" JNIEXPORT jobject JNICALL
Java_com_zipper_gl_1vector_RegionCalculator_calculateRegions(JNIEnv *env, jobject thiz, jobject line_art_bitmap) {

    AndroidBitmapInfo info;
    void *pixels;
    int ret;

    // 获取Bitmap信息
    if ((ret = AndroidBitmap_getInfo(env, line_art_bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return nullptr;
    }

    // 检查Bitmap格式，目前只支持 ARGB_8888
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return nullptr;
    }

    // 锁定像素，直接访问像素数据
    if ((ret = AndroidBitmap_lockPixels(env, line_art_bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return nullptr;
    }

    long time = std::time(nullptr);
    LOGD("开始计算");
    auto *lineArtPixels = (uint32_t *) pixels; // 原始线稿像素
    int width = static_cast<int>(info.width);
    int height = static_cast<int>(info.height);

    // 创建一个新的像素缓冲区用于存储蒙版结果
    // 注意：这里分配了新的内存，需要确保在使用完后释放
    auto *maskPixels = new uint32_t[width * height];
    std::vector<int> regionIds(width * height, -1); // -1: NOT_VISITED

    int currentRegionId = 0;
    std::queue<Point> q;

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            uint32_t index = y * width + x;
            uint32_t pixel = lineArtPixels[index];

            if (isLineColor(pixel, lineColorThreshold)) {
                // 线条，不作为可填充区域
                regionIds[index] = -1;
                // 蒙版图上线条区域设置为黑色
                maskPixels[index] = 0xFF000000; // ARGB_8888 黑色
                continue;
            }

            if (regionIds[index] == -1) { // 如果未访问过
                q.push({x, y});
                regionIds[index] = currentRegionId;

                while (!q.empty()) {
                    Point p = q.front();
                    q.pop();

                    // 检查相邻像素 (上、下、左、右)
                    int dx[] = {0, 0, 1, -1};
                    int dy[] = {1, -1, 0, 0};

                    for (int i = 0; i < 4; ++i) {
                        int nx = p.x + dx[i];
                        int ny = p.y + dy[i];
                        uint32_t nIndex = ny * width + nx;

                        if (nx >= 0 && nx < width && ny >= 0 && ny < height &&
                            regionIds[nIndex] == -1) { // 边界内且未访问过
                            uint32_t neighborPixel = lineArtPixels[nIndex];

                            if (!isLineColor(neighborPixel, lineColorThreshold)) {
                                regionIds[nIndex] = currentRegionId;
                                q.push({nx, ny});
                            }
                        }
                    }
                }
                currentRegionId++;
            }
            // 填充蒙版像素
            // 将区域ID映射到灰度颜色，限制在0-255
//            int grayValue = (regionIds[index] == -1) ? 0 : (regionIds[index] * 30) % 255;
//            maskPixels[index] = (0xFF << 24) | (grayValue << 16) | (grayValue << 8) | grayValue; // ARGB_8888 灰度
            maskPixels[index] = colorGenerator2.getColor(regionIds[index]);
        }
    }
    long end = std::time(nullptr) - time;
    LOGD("计算完成, 耗时 = %ld", end);

    // 解锁原始Bitmap的像素
    AndroidBitmap_unlockPixels(env, line_art_bitmap);

    // 创建一个新的Bitmap对象并设置像素
    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                          "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    // 获取 Bitmap.Config 枚举的实例
    jclass configClass = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID argb8888Field = env->GetStaticFieldID(configClass, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
    jobject argb8888Config = env->GetStaticObjectField(configClass, argb8888Field);

    jobject resultBitmap = env->CallStaticObjectMethod(bitmapClass, createBitmapMethod, width, height, argb8888Config);

    // 锁定新Bitmap的像素，将C++处理后的数据复制过去
    void *resultPixels;
    if ((ret = AndroidBitmap_lockPixels(env, resultBitmap, &resultPixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed on resultBitmap ! error=%d", ret);
        delete[] maskPixels; // 释放内存
        return nullptr;
    }
    memcpy(resultPixels, maskPixels, width * height * sizeof(uint32_t));

    AndroidBitmap_unlockPixels(env, resultBitmap);

    delete[] maskPixels; // 释放C++中分配的像素内存

    return resultBitmap;
}