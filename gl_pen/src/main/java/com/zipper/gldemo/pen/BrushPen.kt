package com.zipper.gldemo.pen

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PointF
import com.zipper.gl.base.GLTexture
import com.zipper.gl.base.OrthographicCamera
import com.zipper.gl.base.distance
import com.zipper.gl.base.normalize
import com.zipper.gl.base.normalizeNdcX
import com.zipper.gl.base.normalizeNdcY
import kotlin.math.floor
import kotlin.math.hypot

/**
 * 笔刷
 */
class BrushPen(
    private val context: Context,
    val config: BrushConfig,
    private val camera: OrthographicCamera
) {

    private val viewWidth get() = camera.viewportWidth
    private val viewHeight get() = camera.viewportHeight
    private val brushTexture = GLTexture()

    private var lastProcessGlX = 0f
    private var lastProcessGlY = 0f

    private var accumulatedDistance = 0f

    fun initialize() {
        brushTexture.upload(BitmapFactory.decodeStream(context.assets.open(config.brushPath)))
    }

    fun getBrushTexture(): GLTexture = brushTexture

    fun resetInterpolationState() {
        lastProcessGlX = 0f
        lastProcessGlY = 0f
        accumulatedDistance = 0f
    }

    fun onScroll(lastX: Float, lastY: Float, curX: Float, curY: Float, onPoint: (BrushPoint) -> Unit) {
        val isDown = lastX == curX
        if (isDown) {
            val wordVertex = camera.screenToWorld(lastX, lastY)
            lastProcessGlX = wordVertex[0]
            lastProcessGlY = wordVertex[1]
            accumulatedDistance = 0f
            onPoint(BrushPoint(wordVertex[0], wordVertex[1]))
            return
        }

        val worldEnd = camera.screenToWorld(curX, curY)
        val worldCurX = worldEnd[0]
        val worldCurY = worldEnd[1]
        val deltaX = worldCurX - lastProcessGlX
        val deltaY = worldCurY - lastProcessGlY
        val distanceToLastProcessed = hypot(deltaX, deltaY)

        // 累加距离
        accumulatedDistance += distanceToLastProcessed

        val spacingNdc = 0.074f

        while (accumulatedDistance >= spacingNdc) {
            // 已处理的点到当前点之间的距离
            val remainingPathLengthToCurrent = hypot(worldCurX - lastProcessGlX, worldCurY - lastProcessGlY)
            if (remainingPathLengthToCurrent == 0f) {
                break
            }
            val ratio = spacingNdc / remainingPathLengthToCurrent
            val interpolateNdcX = lastProcessGlX + (worldCurX - lastProcessGlX) * ratio
            val interpolateNdcY = lastProcessGlY + (worldCurY - lastProcessGlY) * ratio
            onPoint(BrushPoint(interpolateNdcX, interpolateNdcY))
            lastProcessGlX = interpolateNdcX
            lastProcessGlY = interpolateNdcY
            accumulatedDistance -= spacingNdc
        }


    }


    fun generatePoints2(startX: Float, startY: Float, endX: Float, endY: Float, onPoint: (BrushPoint) -> Unit) {
        // 1. 将输入的所有View坐标转换为World坐标
        val worldStart = camera.screenToWorld(startX, startY)
        val worldStartX = worldStart[0]
        val worldStartY = worldStart[1]
        if (startX == endX) {
            onPoint(BrushPoint(worldStartX, worldStartY))
            return
        }

        val worldEnd = camera.screenToWorld(endX, endY)
        val worldEndX = worldEnd[0]
        val worldEndY = worldEnd[1]

        val modelDistance = distance(worldStartX, worldStartY, worldEndX, worldEndY)

        val modelStep = config.pixInterval / camera.pixelsPerModelUnit

        // 根据笔刷的屏幕间距，计算要插值的点
        val numberOfPointsToInsert = floor(modelDistance / modelStep).toInt()

        val pointCount = numberOfPointsToInsert
        if (pointCount == 1) {
            // 不需要插入时，使用起始点
            onPoint(BrushPoint(worldStartX, worldStartY))
        } else if (pointCount == 2) {
            // 插入起始点
            onPoint(BrushPoint(worldStartX, worldStartY))
            // 插入结束点
            onPoint(BrushPoint(worldEndX, worldEndY))
        } else {
            // 4. 在World坐标系下进行插值
            for (i in 1 until pointCount) {
                val t = i.toFloat() / (pointCount + 1)
                val interpolatedWorldX = worldStartX + t * (worldEndX - worldStartX)
                val interpolatedWorldY = worldStartY + t * (worldEndY - worldStartY)

                // 直接生成世界坐标点，不再需要复杂的范围检查和归一化
                onPoint(BrushPoint(interpolatedWorldX, interpolatedWorldY))
            }
        }
    }

    fun generatePoints(startX: Float, startY: Float, endX: Float, endY: Float, onPoint: (BrushPoint) -> Unit) {
        // 世界坐标系最小范围
        val minR = camera.getMinRange()
        // 世界坐标系最大范围
        val maxR = camera.getMaxRange()
        val d = distance(startX, startY, endX, endY)
        val pointCount = (d / config.pixInterval).toInt()

        if (pointCount == 1) {
            val point = createBrushPointInRange(startX, startY, viewWidth, viewHeight, minR, maxR)
            if (point != null) {
                onPoint(point)
            }
        } else if (pointCount == 2) {
            var point = createBrushPointInRange(startX, startY, viewWidth, viewWidth, minR, maxR)
            if (point != null) {
                onPoint(point)
            }
            point = createBrushPointInRange(endX, endY, viewWidth, viewWidth, minR, maxR)
            if (point != null) {
                onPoint(point)
            }
        } else {
            for (i in 0 until pointCount) {
                val t = i * 1.0f / (pointCount)
                val point = createBrushPointInRange(
                    startX + t * (endX - startX),
                    startY + t * (endY - startY),
                    viewWidth, viewHeight,
                    minR, maxR
                )
                if (point != null) {
                    onPoint(point)
                }
            }
        }
    }

    /**
     * 计算顶点位置，是否在画布范围内，并返回当前的坐标
     */
    private fun createBrushPointInRange(x: Float, y: Float, w: Int, h: Int, minR: FloatArray, maxR: FloatArray): BrushPoint? {
        if (w == 0 || h == 0) {
            return null
        }
        // Log.d("BAAA", "createBrushPointInRange: $x, $y")
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

    fun generateVertexForCurve(
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        controlX: Float, controlY: Float,
    ) {
        val curveLength = getQuadraticBezierLength(startX, startY, endX, endY, controlX, controlY)

    }


    fun getQuadraticBezierLength(
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        controlX: Float, controlY: Float, segments: Int = 10
    ): Float {
        var length = 0f
        var lastPointX = startX
        var lastPointY = startY

        for (i in 1..segments) {
            val t = i.toFloat() / segments
            val oneMinusT = 1 - t
            val x = oneMinusT * oneMinusT * startX + 2 * oneMinusT * t * controlX + t * t * endX
            val y = oneMinusT * oneMinusT * startY + 2 * oneMinusT * t * controlY + t * t * endY
            length += distance(lastPointX, lastPointY, x, y)
            lastPointX = x
            lastPointY = y
        }
        return length
    }

    /**
     * 二次贝塞尔曲线公式：B(t) = (1-t)²P₀ + 2(1-t)tP₁ + t²P₂
     * 其中 P₀是起点，P₁是控制点，P₂是终点，t∈[0,1]
     */
    fun getQuadraticBezierPoint(
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        controlX: Float, controlY: Float,
        t: Float, result: PointF
    ) {
        val oneMinusT = 1 - t
        val x = oneMinusT * oneMinusT * startX + 2 * oneMinusT * t * controlX + t * t * endX
        val y = oneMinusT * oneMinusT * startY + 2 * oneMinusT * t * controlY + t * t * endY
        result.set(x, y)

    }

}