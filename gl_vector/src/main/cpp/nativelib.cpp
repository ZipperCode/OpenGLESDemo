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


const int lineColorThreshold = 200;
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
    uint8_t r = (hash >> 16) & 0xFF;
    uint8_t g = (hash >> 8) & 0xFF;
    uint8_t b = hash & 0xFF;
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
            {0.70710678, -0.5,       0.5},
            {0.5,        0.70710678, 0.5},
            {-0.5,       -0.5,       0.70710678}
    };
    // 缓存已生成的颜色
    std::unordered_map<int, uint32_t> colorCache;

    // 3D空间坐标转换
    void transformCoordinates(int index, int &x, int &y, int &z) {
        // 使用质数确保均匀分布
        const int p1 = 73856093;
        const int p2 = 19349663;
        const int p3 = 83492791;

        // 3D康威函数，将1D映射到3D
        x = (index * p1) ^ ((index >> 16) * p2);
        y = (index * p2) ^ ((index >> 12) * p3);
        z = (index * p3) ^ ((index >> 8) * p1);

        // 应用旋转矩阵，增加随机性
        auto tx = static_cast<float>(x);
        auto ty = static_cast<float>(y);
        auto tz = static_cast<float>(z);
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
        uint8_t r = (baseColor >> 16) & 0xFF;
        uint8_t g = (baseColor >> 8) & 0xFF;
        uint8_t b = baseColor & 0xFF;

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
    if (a < 150) {
        return false;
    }
    uint8_t r = (pixel >> 16) & 0xFF; // ARGB_8888
    uint8_t g = (pixel >> 8) & 0xFF;
    uint8_t b = pixel & 0xFF;
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
extern "C"
JNIEXPORT jint JNICALL
Java_com_zipper_gl_1vector_RegionCalculator_regionGenerate(JNIEnv *env, jobject thiz, jobject line_art_bitmap, jobject mask_bitmap) {
    AndroidBitmapInfo info;
    AndroidBitmapInfo maskInfo;
    void *pixels;
    void *pixels2;
    int ret;
    // 获取Bitmap信息
    if ((ret = AndroidBitmap_getInfo(env, line_art_bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return -1;
    }

    // 检查Bitmap格式，目前只支持 ARGB_8888
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return -1;
    }

    // 获取Bitmap信息
    if ((ret = AndroidBitmap_getInfo(env, mask_bitmap, &maskInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return -1;
    }

    // 检查Bitmap格式，目前只支持 ARGB_8888
    if (maskInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return -1;
    }

    // 检查Bitmap尺寸
    if (info.width != maskInfo.width || info.height != maskInfo.height) {
        LOGE("Bitmap size is not equal to input image size !");
        return -1;
    }

    // 锁定像素，直接访问像素数据
    if ((ret = AndroidBitmap_lockPixels(env, line_art_bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return -1;
    }
    if ((ret = AndroidBitmap_lockPixels(env, mask_bitmap, &pixels2)) < 0) {
        AndroidBitmap_unlockPixels(env, line_art_bitmap);
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return -1;
    }

    LOGD("线稿区域生成，开始");
    int width = static_cast<int>(info.width);
    int height = static_cast<int>(info.height);

    auto *linePixels = static_cast<uint32_t *>(pixels);
    auto *maskPixels = static_cast<uint32_t *>(pixels2);

    std::vector<int> regionIds(width * height, -1);

    int currentRegionId = 0;
    std::queue<Point> q;
    std::unordered_map<uint32_t, int> regionColorCount;


    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            uint32_t index = y * width + x;
            uint32_t pixel = linePixels[index];

            if (isLineColor(pixel, lineColorThreshold)) {
                // 线条，不作为可填充区域
                regionIds[index] = -1;
                // 蒙版图上线条区域设置为黑色
                maskPixels[index] = 0xFF000000; // ARGB_8888 黑色
                continue;
            }
            if (regionIds[index] == -1) {
                // 未访问过的区域
                q.push({x, y});
                regionIds[index] = currentRegionId;
                int areaCount = 0;

                while (!q.empty()) {
                    auto p = q.front();
                    q.pop();
                    areaCount++;

                    // 检查相邻像素 (上、下、左、右)
                    int dx[] = {0, 0, 1, -1};
                    int dy[] = {1, -1, 0, 0};

                    for (int i = 0; i < 4; i++) {
                        int nx = p.x + dx[i];
                        int ny = p.y + dy[i];
                        uint32_t nIndex = ny * width + nx;

                        if (nx >= 0 && nx < width && ny >= 0 && ny < height && regionIds[nIndex] == -1) {
                            uint32_t neighborPixel = linePixels[nIndex];
                            if (!isLineColor(neighborPixel, lineColorThreshold)) {
                                regionIds[nIndex] = currentRegionId;
                                q.push(Point({nx, ny}));
                            }
                        }
                    }
                }
                if (areaCount < 50) {
                    LOGD("区域数量 = %d", areaCount);
                    regionColorCount.insert({colorGenerator2.getColor(regionIds[index]), areaCount});
                    // regionIdCount.insert({currentRegionId})
                }

                // 区域都访问完了
                currentRegionId++;
            }
            int regionId = regionIds[index];
            if (regionId == -1) {
                maskPixels[index] = 0xFF000000;
            } else {
                // 填充到蒙版
                maskPixels[index] = colorGenerator2.getColor(regionIds[index]);
            }
        }
    }

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            uint32_t index = y * width + x;
            uint32_t pixel = maskPixels[index];
            int count = regionColorCount[pixel];
            if(count > 0) {
                maskPixels[index] = 0xFF000000;
            }
        }
    }

    // 解锁像素
    AndroidBitmap_unlockPixels(env, line_art_bitmap);
    AndroidBitmap_unlockPixels(env, mask_bitmap);

    return 0;
}