//
// Created by Zipper on 2025/7/5.
//

#ifndef PRINCESSCOLOR_AREA_GENERATOR_H
#define PRINCESSCOLOR_AREA_GENERATOR_H

#include <cstdint>
#include <unordered_map>

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
class ColorGenerator {
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

#endif //PRINCESSCOLOR_AREA_GENERATOR_H
