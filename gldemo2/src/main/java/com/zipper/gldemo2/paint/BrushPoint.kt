package com.zipper.gldemo2.paint

import kotlin.math.sqrt

class BrushPoint @JvmOverloads constructor(
    var x: Float = 0f,
    var y: Float = 0f
) {

    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun set(other: BrushPoint) {
        this.x = other.x
        this.y = other.y
    }

    fun distanceTo(other: BrushPoint): Float {
        return sqrt((this.x - other.x) * (this.x - other.x) + (this.y - other.y) * (this.y - other.y))
    }

    /**
     * 当前点与另一个点的加权平均点
     */
    fun multiplySum(other: BrushPoint, scalar: Float = 0.5f): BrushPoint {
        return BrushPoint((this.x + other.x) * scalar, (this.y + other.y) * scalar)
    }

    /**
     * 将两个点的加权平均点赋值给当前点
     */
    fun setMultiplySum(point1: BrushPoint, point2: BrushPoint, scalar: Float = 0.5f) {
        this.x = (point1.x + point2.x) * scalar
        this.y = (point1.y + point2.y) * scalar
    }

    override fun toString(): String {
        return "BrushPoint(x=$x, y=$y)"
    }


}