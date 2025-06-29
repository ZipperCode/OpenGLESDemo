package com.zipper.gl_vector.math

import java.lang.Float.floatToIntBits

interface Vector<T : Vector<T>> {
    /**
     * 拷贝
     */
    fun copy(): T

    /**
     * 欧几里得距离
     */
    fun length(sqrt: Boolean = true): Float

    /**
     * 限制向量长度
     * @param limit 限制长度
     */
    fun limit(limit: Float): T

    /**
     * 限制向量的范围
     * @param min 最小值
     * @param max 最大值
     */
    fun clamp(min: Float, max: Float): T

    /**
     * 根据一个向量设置
     */
    fun set(v: T): T

    /**
     * 向量加法
     */
    fun add(v: T): T

    /**
     * 向量减法
     */
    fun sub(v: T): T

    /**
     * 归一化
     */
    fun normalize(): T

    /**
     * 点
     */
    fun dot(v: T): Float

    /**
     * 所发
     */
    fun scale(s: Float): T

    /**
     * 距离
     */
    fun distance(v: T): Float

    /**
     * 是否是给定范围内的单位向量
     * @param margin 范围
     */
    fun isUnit(margin: Float = 0.000000001f): Boolean

    /**
     * 是否零向量
     * @param margin 零向量的范围
     */
    fun isZero(margin: Float = 0.0001f): Boolean

    /**
     * 向量是否模糊平等
     */
    fun epsilonEquals(v: T, epsilon: Float): Boolean

}