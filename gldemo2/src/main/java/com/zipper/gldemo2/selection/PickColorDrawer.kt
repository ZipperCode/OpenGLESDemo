package com.zipper.gldemo2.selection

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/**
 *
 * @author  zhangzhipeng
 * @date    2024/12/30
 */
class PickColorDrawer {

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.GRAY
        strokeWidth = 10f
        isAntiAlias = true
    }

    fun drawSelection(canvas: Canvas, rect: RectF) {
        canvas.drawPoint(rect.centerX(), rect.centerY(), paint)
        canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() / 4f, paint)
    }
}