package com.zipper.gldemo2.paint

import android.content.Context
import android.graphics.Color
import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.util.Log
import android.view.ViewConfiguration
import com.zipper.gldemo2.OpenGLHelper

class BrushPen(
    private val context: Context,
    private val brushRenderer: BrushRenderer
) {

    private val fromPoint = BrushPoint()
    private val toPoint = BrushPoint()
    private val controlPoint = BrushPoint()

    // 上一个点
    private val lastPoint = BrushPoint()

    // 当前点
    private val currentPoint = BrushPoint()

    // 按下点
    private val downPoint = BrushPoint()

    // 移动点
    private val movePoints = ArrayList<Float>()
    private var paintPoints = ArrayList<Float>()

    private val paintStack = ArrayList<PaintPoints>()

    private var currentPaints: PaintPoints? = null

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var hasMoved = false

    var pointSize = 100f

    var penColor = Color.RED


    fun onActionDown(point: BrushPoint) {
        fromPoint.set(point)
        toPoint.set(point)
        downPoint.set(point)
        lastPoint.set(point)
        controlPoint.set(point)
        currentPaints = PaintPoints().apply {
            color = penColor
        }
        hasMoved = false
    }

    fun onActionMove(eventPoint: BrushPoint, viewWidth: Int, viewHeight: Int) {
        val distance = fromPoint.distanceTo(eventPoint)
        if (distance < touchSlop) {
            // 小于移动距离
            return
        }
        hasMoved = true
        currentPoint.set(eventPoint)
        // 终点设置为上个点和当前点的加权点
        toPoint.setMultiplySum(lastPoint, currentPoint)
        // 以上一次为控制点
        controlPoint.set(lastPoint)

        val list = PointHelper.smoothPoints(fromPoint, toPoint, controlPoint, pointSize)
        list.forEach {
//            Log.d("BAAA", "point = ${it}")
            val points = OpenGLHelper.convertGlVertex(it.x / viewWidth, it.y / viewHeight)
            movePoints.add(points[0])
            movePoints.add(points[1])
        }
//        Log.e("BAAA", "=================================")
        if (movePoints.isEmpty()) {
            return
        }
        paintPoints.addAll(movePoints)
        brushRenderer.drawPoints(movePoints)
        movePoints.clear()

        fromPoint.set(toPoint)
        lastPoint.set(eventPoint)
    }

    fun onActionUp(eventPoint: BrushPoint, viewWidth: Int, viewHeight: Int) {
        if (!hasMoved) {
            val points = OpenGLHelper.convertGlVertex(eventPoint.x / viewWidth, eventPoint.y / viewHeight)
            paintPoints.add(points[0])
            paintPoints.add(points[1])
            brushRenderer.drawPoints(paintPoints)
            Log.d("BAAA", "onActionUp#eventPoint = ${eventPoint}")
        }
        if (currentPaints != null) {
            brushRenderer.saveFrame(paintPoints)
            currentPaints!!.points.addAll(paintPoints)
            paintStack.add(currentPaints!!)
            paintPoints.clear()
            currentPaints = null
        }
    }

    class PaintPoints {
        var points = ArrayList<Float>()
        var color = Color.RED
    }
}