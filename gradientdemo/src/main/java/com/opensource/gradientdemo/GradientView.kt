package com.opensource.gradientdemo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

/**
 *
 * @author  zhangzhipeng
 * @date    2025/1/6
 */
class GradientView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var currentColor = Color.RED

    private val downList: MutableList<ColorPoint> = mutableListOf()

    private val paint = Paint().apply {

    }

    private val viewSize = RectF()
    private val vertexPoint = mutableListOf<PointF>()

    private val dotPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    fun reset() {
        currentColor = Color.RED
        downList.clear()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        vertexPoint.clear()
        vertexPoint.add(PointF(0f, 0f))
        vertexPoint.add(PointF(0f, h.toFloat()))
        vertexPoint.add(PointF(w.toFloat(), 0f))
        vertexPoint.add(PointF(w.toFloat(), h.toFloat()))
        viewSize.set(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val lastColor = downList.lastOrNull()?.color
                if (lastColor == currentColor) {
                    return true
                }
                downList.add(ColorPoint(currentColor, event.x, event.y, distanceTopRatio(event.x, event.y)))
                downList.sortWith{ o1, o2 -> (o1.ratio * 100 - o2.ratio * 100).toInt() }
            }

            MotionEvent.ACTION_UP -> {
                invalidate()
            }
        }
        return true
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (downList.isEmpty()) {
            return
        } else if (downList.size > 1) {
            val color = IntArray(downList.size)
            val position = FloatArray(downList.size)
            for (i in 0 until downList.size) {
                color[i] = downList[i].color
                position[i] = distanceTopRatio(downList[i].x, downList[i].y)
            }

            paint.shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                color,
                position,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        } else {
            val first = downList.first()
            paint.shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                first.color,
                first.color,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }

//        for (i in 0 until downList.size) {
//            val nextIndex = (i + 1) % downList.size
//            val point = findNearest(downList[i])
//            val nextPoint = findNearest(downList[nextIndex])
//            paint.shader = LinearGradient(point.x, point.y, nextPoint.x, nextPoint.y, point.color, nextPoint.color, Shader.TileMode.CLAMP)
//            val rect = RectF(point.x, point.y, nextPoint.x, nextPoint.y)
//            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
////                canvas.drawRect(rect, paint)
//
//            canvas.drawPoint(point.x, point.y, dotPaint)
//            canvas.drawPoint(nextPoint.x, nextPoint.y, dotPaint)
//        }
    }

    fun distanceTopRatio(x: Float, y: Float): Float {
        val d = distance(0f, 0f, x, y)
        val line = sqrt(width * width.toFloat() + height * height)
        return d / line
    }

    fun findNearest(point: ColorPoint): ColorPoint {
        var minDistance = Float.MAX_VALUE
        var nearPoint = ColorPoint(point.color, 0f, 0f)

        for (p in vertexPoint) {
            val d = distance(p.x, p.y, point.x, point.y)
            if (d < minDistance) {
                minDistance = d
                nearPoint = ColorPoint(point.color, p.x, p.y)
            }
        }

        return nearPoint
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    class ColorPoint(
        val color: Int,
        val x: Float,
        val y: Float,
        val ratio: Float = 0f
    )
}