package com.zipper.gldraw.math

/**
 *
 * @author  zhangzhipeng
 * @date    2025/6/24
 */
interface Vector<T : Vector<T>> {
    /**
     * 拷贝
     */
    fun copy(): T

    /**
     * @return 欧几里得长度
     */
    fun length(): Float

    /**
     * 裁剪向量最大最小值
     */
    fun clamp(min: Float, max: Float): T

    fun set(v: T): T

    /**
     * 加法
     */
    fun plus(v: T): T

    /**
     * 减法
     */
    fun minus(v: T): T

    /**
     * 点
     */
    fun dot(v: T): Float

    /**
     * 缩放
     * @param  scalar 缩放值
     */
    fun scale(scalar: Float): T

    /**
     * 归一化
     */
    fun normalize(): T

    /**
     * 返回与其他向量的距离
     */
    fun distance(v: T): Float

    /**
     * 线性插值在该矢量和目标向量的alpha之间，该量在[0,1]范围内。结果存储在该向量中。
     */
    fun linearInterpolate(target: T, alpha: Float): T

    /**
     * 矢量是否等于目标向量
     */
    fun epsilonEquals(target: T, epsilon: Float): Boolean

    /**
     * 矢量是否为单位矢量
     */
    fun isUnit(margin: Float = 0.000000001f): Boolean

    /**
     * 矢量是否空
     */
    fun isZero(): Boolean
}