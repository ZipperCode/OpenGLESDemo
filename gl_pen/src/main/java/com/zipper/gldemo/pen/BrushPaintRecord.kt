package com.zipper.gldemo.pen

import android.graphics.Color

/**
 * 笔刷轨迹记录
 */
class BrushPaintRecord {
    /**
     * 笔刷名称
     */
    var brushName: String = ""

    /**
     * 笔刷大小
     */
    var brushSize: Float = 1f

    /**
     * 笔刷颜色
     */
    var brushColor = Color.WHITE
    /**
     * 笔刷轨迹
     */
    val brushPoints: MutableList<BrushPoint> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BrushPaintRecord

        if (brushSize != other.brushSize) return false
        if (brushColor != other.brushColor) return false
        if (brushName != other.brushName) return false
        if (brushPoints != other.brushPoints) return false

        return true
    }

    override fun hashCode(): Int {
        var result = brushSize.hashCode()
        result = 31 * result + brushColor
        result = 31 * result + brushName.hashCode()
        result = 31 * result + brushPoints.hashCode()
        return result
    }


}