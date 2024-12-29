package com.zipper.gldemo2.selection

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/**
 * 选区绘制器
 */
class SelectionDrawer {
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.GRAY
        strokeWidth = 2f
        isAntiAlias = true
    }

    /**
     * 绘制选区
     */
    fun drawSelection(canvas: Canvas, rect: RectF) {
        canvas.drawRect(rect, paint)
    }
}
