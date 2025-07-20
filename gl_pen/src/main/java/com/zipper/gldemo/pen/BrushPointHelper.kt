package com.zipper.gldemo.pen

import android.util.Log
import com.zipper.gl.base.distance
import com.zipper.gl.base.normalize
import com.zipper.gl.base.normalizeNdcX
import com.zipper.gl.base.normalizeNdcY
import kotlin.math.sqrt

object BrushPointHelper {

    /**
     * 笔刷缩放转化
     */
    fun convertBrushRenderSize(resSize: Float, brushSize: Float, zoomFactor: Float): Float {
        // 4 20 2 => 20 * (4 / 2)
        return brushSize / sqrt(zoomFactor / resSize)
    }

    fun brushPointCreator(
        startX: Float, startY: Float,
        curX: Float, curY: Float,
        width: Int, height: Int,
        minRange: FloatArray,
        maxRange: FloatArray,
        pixInterval: Int,
        scale: Float,
        onPoint: (BrushPoint) -> Unit
    ) {
        val pointCount = (distance(startX, startY, curX, curY) / pixInterval / scale).toInt()

        if (pointCount <= 1) {
            val point = createBrushPointInRange(startX, startY, width, height, minRange, maxRange)
            if (point != null) {
                onPoint(point)
            }
        } else if (pointCount <= 2) {
            var point = createBrushPointInRange(startX, startY, width, height, minRange, maxRange)
            if (point != null) {
                onPoint(point)
            }
            point = createBrushPointInRange(curX, curY, width, height, minRange, maxRange)
            if (point != null) {
                onPoint(point)
            }
        } else {
            // Log.d("BAAA", "pointCount = $pointCount")
            for (i in 0 until pointCount) {
                val t = i * 1.0f / pointCount
                val point = createBrushPointInRange(
                    startX + t * (curX - startX),
                    startY + t * (curY - startY),
                    width, height,
                    minRange, maxRange
                )
                if (point != null) {
                    onPoint(point)
                }
            }
        }
    }

    /**
     * 两点之前，生成一系列的顶点值
     */
    fun generateBrushPoints(
        startX: Float, startY: Float,
        curX: Float, curY: Float,
        width: Int, height: Int,
        minRange: FloatArray,
        maxRange: FloatArray,
        pixInterval: Int
    ): List<BrushPoint> {
        val pointCount = (distance(startX, startY, curX, curY) / pixInterval).toInt()
        val points = ArrayList<BrushPoint>(pointCount)
        if (pointCount <= 1) {
            val point = createBrushPointInRange(startX, startY, width, height, minRange, maxRange)
            if (point != null) {
                points.add(point)
            }
        } else if (pointCount <= 2) {
            var point = createBrushPointInRange(startX, startY, width, height, minRange, maxRange)
            if (point != null) {
                points.add(point)
            }
            point = createBrushPointInRange(curX, curY, width, height, minRange, maxRange)
            if (point != null) {
                points.add(point)
            }
        } else {
            for (i in 0 until pointCount) {
                val t = i * 1.0f / pointCount
                val point = createBrushPointInRange(
                    startX + t * (curX - startX),
                    startY + t * (curY - startY),
                    width, height,
                    minRange, maxRange
                )
                if (point != null) {
                    points.add(point)
                }
            }
        }

        return points
    }


    /**
     * 计算顶点位置，是否在画布范围内，并返回当前的坐标
     */
    private fun createBrushPointInRange(x: Float, y: Float, w: Int, h: Int, minR: FloatArray, maxR: FloatArray): BrushPoint? {
        if (w == 0 || h == 0) {
            return null
        }
        val glX = (x * 1f / w).normalizeNdcX()
        val glY = (y * 1f / h).normalizeNdcY()
        if (glX in minR[0]..maxR[0] && glY in maxR[1]..minR[1]) {
            return BrushPoint(
                normalize(glX, minR[0], maxR[0]).normalizeNdcX(),
                normalize(glY, minR[1], maxR[1]).normalizeNdcY()
            )
        }

        return null
    }
}