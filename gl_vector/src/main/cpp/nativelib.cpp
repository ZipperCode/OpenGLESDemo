

#include <jni.h>
#include <string>
#include <queue>
#include <unordered_map>

#include <android/log.h>
#include <android/bitmap.h>

#include "area_generator.h"
#include "opengles_blur.h"
#include "image_process.h"

// 定义日志宏
#define LOG_TAG "NativeRegionCalculator"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
struct Point {
    int x, y;
};

int const MIN_AREA_PIXEL_COUNT = 10;
const int lineColorThreshold = 100;

// 全局生成器实例
static ColorGenerator colorGenerator;

bool isLineColor(uint32_t pixel);

uint32_t getLuminanceInt(uint32_t pixel) {
    uint32_t r = (pixel >> 16) & 0xff;
    uint32_t g = (pixel >> 8) & 0xff;
    uint32_t b = pixel & 0xff;
    float luminance = 0.299f * static_cast<float>(r) + 0.587f * static_cast<float>(g) + 0.114f * static_cast<float>(b);
    return static_cast<uint32_t >(luminance);
}

float getLuminance(uint32_t pixel) {
    uint32_t r = (pixel >> 16) & 0xff;
    uint32_t g = (pixel >> 8) & 0xff;
    uint32_t b = pixel & 0xff;
    float luminance = 0.299f * static_cast<float>(r) + 0.587f * static_cast<float>(g) + 0.114f * static_cast<float>(b);
    return luminance / 255.0f;
}

inline float clamp(float value, float min, float max) {
    if (value < min) return min;
    if (value > max) return max;
    return value;
}


// 辅助函数：组合RGBA分量为像素
inline uint32_t makeRGBA(int r, int g, int b, int a) {
    return ((uint32_t) a << 24) | ((uint32_t) r << 16) | ((uint32_t) g << 8) | ((uint32_t) b << 0);
}

// 辅助函数：生成高斯核
std::vector<float> createGaussianKernel(float radius) {
    int size = (int) (radius * 2 + 1); // 核的大小
    if (size % 2 == 0) size++; // 确保是奇数
    std::vector<float> kernel(size * size);
    float sigma = radius / 3.0f; // 经验值，可以调整
    float sum = 0.0f;
    int halfSize = size / 2;

    for (int y = -halfSize; y <= halfSize; ++y) {
        for (int x = -halfSize; x <= halfSize; ++x) {
            float value = (1.0f / (2.0f * M_PI * sigma * sigma)) *
                          exp(-(x * x + y * y) / (2.0f * sigma * sigma));
            kernel[(y + halfSize) * size + (x + halfSize)] = value;
            sum += value;
        }
    }

    // 归一化核
    for (float &val: kernel) {
        val /= sum;
    }
    return kernel;
}

// 高斯模糊处理函数
void applyGaussianBlur(uint32_t *pixels, int width, int height, float radius) {
    std::vector<float> kernel = createGaussianKernel(radius);
    int kernelSize = sqrt(kernel.size());
    int halfKernelSize = kernelSize / 2;

    // 创建一个缓冲区来存储模糊后的像素，避免修改原始像素时影响后续计算
    std::vector<uint32_t> blurredPixels(width * height);

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            float sumR = 0.0f, sumG = 0.0f, sumB = 0.0f, sumA = 0.0f;

            for (int ky = 0; ky < kernelSize; ++ky) {
                for (int kx = 0; kx < kernelSize; ++kx) {
                    int pixelX = x + (kx - halfKernelSize);
                    int pixelY = y + (ky - halfKernelSize);
                    pixelX = pixelX < 0 ? 0 : pixelX >= width ? width - 1 : pixelX;
                    pixelY = pixelY < 0 ? 0 : pixelY >= height ? height - 1 : pixelY;

                    uint32_t pixel = pixels[pixelY * width + pixelX];
                    float kernelValue = kernel[ky * kernelSize + kx];

                    sumA += ((pixel >> 24) & 0xFF) * kernelValue;
                    sumR += ((pixel >> 16) & 0xFF) * kernelValue;
                    sumG += ((pixel >> 8) & 0xFF) * kernelValue;
                    sumB += (pixel & 0xFF) * kernelValue;
                }
            }

            blurredPixels[y * width + x] = makeRGBA(sumA, sumR, sumG, sumB);;
        }
    }

    // 将模糊后的像素复制回原始像素区域
    std::copy(blurredPixels.begin(), blurredPixels.end(), pixels);
}

void storageLineArt(uint32_t *pixels, int width, int height) {
//    applyGaussianBlur(pixels, width, height, 5);
//    GLEnv env;
//    imageBlur(&env, pixels, width, height, 5);
//    storageLineArt(&env, pixels, width, height);
//    imageBlur(&env, pixels, width, height, 2);
//
    // 高斯模糊
    //processBlur(pixels, width, height, 5);
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            uint32_t index = y * width + x;
            uint32_t pixel = pixels[index];
            uint32_t luminance = getLuminanceInt(pixels[index]);
            if (luminance > 150) {
                pixels[index] = 0xFFFFFFFF;
            } else {
                pixels[index] = 0xFF000000;
            }
//            pixels[index] = luminanceToColor(luminance < 50 ? 0 : luminance);
            //LOGD("Luminance: %d, Pixel: %X -> %X", luminance, pixel, pixels[index]);
        }
    }
//    processBlur(pixels, width, height, 3);

//    auto *tempPixels = new uint32_t[width * height];
//    for (int y = 0; y < height; ++y) {
//        for (int x = 0; x < width; ++x) {
//
//            float sumR = 0.0f, sumG = 0.0f, sumB = 0.0f, sumA = 0.0f;
//            float totalWeight = 0.0f;
//
//            // 考虑当前像素及其周围的8个像素（3x3区域）
//            for (int dy = -1; dy <= 1; ++dy) {
//                for (int dx = -1; dx <= 1; ++dx) {
//                    int sampleX = x + dx;
//                    int sampleY = y + dy;
//                    sampleX = sampleX < 0 ? 0 : sampleX >= width ? width - 1 : sampleX;
//                    sampleY = sampleY < 0 ? 0 : sampleY >= height ? height - 1 : sampleY;
//
//                    uint32_t pixel = pixels[sampleY * width + sampleX];
//                    int a = (pixel >> 24) & 0xFF;
//                    int r = (pixel >> 16) & 0xFF;
//                    int g = (pixel >> 8) & 0xFF;
//                    int b = pixel & 0xFF;
//                    // 计算权重：这里使用一个简单的距离倒数作为权重，
//                    // 距离越近权重越大。也可以使用高斯函数作为权重。
//                    float distance = sqrtf(static_cast<float>(dx * dx + dy * dy));
//                    float weight = (distance == 0.0f) ? 1.0f : 1.0f / distance; // 中心点权重最大
//
//                    sumA += a * weight;
//                    sumR += r * weight;
//                    sumG += g * weight;
//                    sumB += b * weight;
//                    totalWeight += weight;
//                }
//            }
//
//            // 避免除以零
//            if (totalWeight == 0.0f) totalWeight = 1.0f; // 理论上不会发生
//
//            int avgA = static_cast<int>(sumA / totalWeight);
//            int avgR = static_cast<int>(sumR / totalWeight);
//            int avgG = static_cast<int>(sumG / totalWeight);
//            int avgB = static_cast<int>(sumB / totalWeight);
//
//            // 钳制到0-255范围
//            avgA = std::max(0, std::min(255, avgA));
//            avgR = std::max(0, std::min(255, avgR));
//            avgG = std::max(0, std::min(255, avgG));
//            avgB = std::max(0, std::min(255, avgB));
//
//            tempPixels[y * width + x] = makeRGBA(avgR, avgG, avgB, avgA);
//
//        }
//    }
//    memcpy(pixels, tempPixels, width * height * sizeof(uint32_t));
//    delete[] tempPixels;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zipper_gl_1vector_RegionCalculator_regionGenerate(JNIEnv *env, jobject thiz, jobject line_art_bitmap, jobject mask_bitmap) {
    AndroidBitmapInfo lineInfo;
    AndroidBitmapInfo maskInfo;
    int ret;
    // 获取图片信息
    if ((ret = AndroidBitmap_getInfo(env, line_art_bitmap, &lineInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return -1;
    }
    if ((ret = AndroidBitmap_getInfo(env, mask_bitmap, &maskInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return -1;
    }

    // 检查Bitmap格式，目前只支持 ARGB_8888
    if (lineInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("LineBitmap format is not RGBA_8888 !");
        return -1;
    }
    if (maskInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("MaskBitmap format is not RGBA_8888 !");
        return -1;
    }

    // 检查Bitmap尺寸
    if (lineInfo.width != maskInfo.width || lineInfo.height != maskInfo.height) {
        LOGE("LineBitmap and MaskBitmap size is not equal to input image size !");
        return -1;
    }

    void *pixels, *pixels2;

    // 锁定像素，直接访问像素数据
    if ((ret = AndroidBitmap_lockPixels(env, line_art_bitmap, &pixels)) < 0) {
        LOGE("LineBitmap AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return -1;
    }
    if ((ret = AndroidBitmap_lockPixels(env, mask_bitmap, &pixels2)) < 0) {
        AndroidBitmap_unlockPixels(env, line_art_bitmap);
        LOGE("MaskBitmap AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return -1;
    }
    LOGD("开始");
    int width = static_cast<int>(lineInfo.width);
    int height = static_cast<int>(lineInfo.height);
    auto *linePixels = static_cast<uint32_t *>(pixels);
    auto *maskPixels = static_cast<uint32_t *>(pixels2);
    // 区域标记
    std::vector<int> regionIds(width * height, -1);
    int currentRegionId = 0;
    std::queue<Point> areaPointQueue;
    // 记录颜色对应的区域像素数量
    std::unordered_map<uint32_t, int> regionColorCount;

    // 检查相邻像素 (上、下、左、右)
    int dx[] = {0, 0, 1, -1};
    int dy[] = {1, -1, 0, 0};

    // 格式化
    storageLineArt(linePixels, width, height);

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            uint32_t index = y * width + x;
            uint32_t pixel = linePixels[index];
            if (isLineColor(pixel)) {
                // 线条
                regionIds[index] = -1;
                uint32_t a = (pixel >> 24) & 0xFF;
                maskPixels[index] = (a << 24);
                continue;
            }

            if (regionIds[index] == -1) {
                areaPointQueue.push({x, y});
                regionIds[index] = currentRegionId;
                // 当前区域像素数量
                int areaPixelCount = 0;

                while (!areaPointQueue.empty()) {
                    auto p = areaPointQueue.front();
                    areaPointQueue.pop();
                    areaPixelCount++;

                    for (int i = 0; i < 4; ++i) {
                        int nx = p.x + dx[i];
                        int ny = p.y + dy[i];
                        uint32_t nIndex = ny * width + nx;
                        // 边界检查且未被访问过的区域
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height && regionIds[nIndex] == -1) {
                            uint32_t neighborPixel = linePixels[nIndex];
                            float luminance = getLuminance(neighborPixel);
//                            if (luminance >= 0.5) {
//                                neighborPixel = 0xFFFFFFF;
//                            }
                            // 未访问过的区域，是线条则跳过
                            if (isLineColor(neighborPixel)) {
                                continue;
                            }
                            regionIds[nIndex] = currentRegionId;
                            // 区域匹配
                            areaPointQueue.push({nx, ny});
                        }
                    }
                }

                if (areaPixelCount < MIN_AREA_PIXEL_COUNT) {
                    // 区域数量小的区域， 记录起来 color ： count
                    regionColorCount.insert({colorGenerator.getColor(regionIds[index]), areaPixelCount});
                }
                // 当前区域访问完了
                currentRegionId++;
            }

            int regionId = regionIds[index];
            if (regionId == -1) {
                // 线条，保持原样
                maskPixels[index] = linePixels[index];
            } else {
                // 填充
                maskPixels[index] = colorGenerator.getColor(regionId);
            }
        }
    }
    LOGD("区域个数 = %d", currentRegionId);

    // 小区域直接填充黑色
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            uint32_t index = y * width + x;
            uint32_t pixel = maskPixels[index];
            int count = regionColorCount[pixel];
            if (count > 0) {
                maskPixels[index] = 0xFF000000;
            }
        }
    }

    // 解锁像素
    AndroidBitmap_unlockPixels(env, line_art_bitmap);
    AndroidBitmap_unlockPixels(env, mask_bitmap);

    return 0;
}

bool isLineColor(uint32_t pixel) {
    uint32_t a = pixel >> 24;
    if (a < 2) {
        return false;
    }
    uint32_t r = (pixel >> 16) & 0xff;
    uint32_t g = (pixel >> 8) & 0xff;
    uint32_t b = pixel & 0xff;
//    float luminance = 0.299f * static_cast<float>(r) + 0.587f * static_cast<float>(g) + 0.114f * static_cast<float>(b);
//    return luminance <= 1.0;
    return r < lineColorThreshold && g < lineColorThreshold && b < lineColorThreshold;
}

//bool isLineColor(uint32_t pixel) {
//    uint8_t a = (pixel >> 24) & 0xFF;
//    if (a < 150) {
//        return false;
//    }
//    uint8_t r = (pixel >> 16) & 0xFF; // ARGB_8888
//    uint8_t g = (pixel >> 8) & 0xFF;
//    uint8_t b = pixel & 0xFF;
//    return r < lineColorThreshold && g < lineColorThreshold && b < lineColorThreshold;
//}