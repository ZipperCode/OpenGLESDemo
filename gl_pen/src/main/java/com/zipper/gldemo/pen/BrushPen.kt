package com.zipper.gldemo.pen

import android.content.Context
import android.graphics.BitmapFactory
import com.zipper.gl.base.GLColor
import com.zipper.gl.base.GLTexture
import com.zipper.gl.base.OrthographicCamera
import com.zipper.gl.base.distance
import com.zipper.gl.base.normalize
import com.zipper.gl.base.normalizeNdcX
import com.zipper.gl.base.normalizeNdcY

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


    fun initialize() {
        brushTexture.upload(BitmapFactory.decodeStream(context.assets.open(config.brushPath)))
    }

    fun getBrushTexture(): GLTexture = brushTexture

    fun generatePoints(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        brushSize: Float,
        glColor: GLColor,
        onPoint: (BrushVertex) -> Unit
    ) {
        // 1. 将输入的所有View坐标转换为World坐标
        val worldStart = camera.screenToWorld(startX, startY)
        val worldEnd = camera.screenToWorld(endX, endY)

        val worldStartX = worldStart[0]
        val worldStartY = worldStart[1]
        val worldEndX = worldEnd[0]
        val worldEndY = worldEnd[1]

        // 2. 在World坐标系下计算两点间的距离
        val worldDistance = distance(worldStartX, worldStartY, worldEndX, worldEndY)

        // 3. 计算需要插入的点的数量
        // 关键点：我们想保持“屏幕上”的视觉密度恒定。
        // 一个单位的世界距离，在屏幕上会显示为 (1.0 * camera.scale) 的长度。
        // 所以，两点在屏幕上显示的像素距离约等于 (worldDistance * camera.scale * (viewWidth/2)) (粗略估算)
        // 我们需要计算出在世界坐标系下，对应于 `screenSpacePixelInterval` 的距离是多少。
        // 一个NDC单位长度大约是 viewWidth/2 像素。
        val worldSpaceInterval = config.pixInterval * (2f / viewWidth)

        // 因此，需要的点数量就是世界距离除以世界坐标下的点间距
        val pointCount = (worldDistance / worldSpaceInterval).toInt()

        if (pointCount <= 1) {
            // 使用起始点
            onPoint(BrushVertex.obtain(worldStartX, worldStartY, brushSize, glColor.r, glColor.g, glColor.b))
        } else if (pointCount <= 2) {
            // 插入起始点
            onPoint(BrushVertex.obtain(worldStartX, worldStartY, brushSize, glColor.r, glColor.g, glColor.b))
            // 插入结束点
            onPoint(BrushVertex.obtain(worldStartX, worldStartY, brushSize, glColor.r, glColor.g, glColor.b))
        } else {
            // 4. 在World坐标系下进行插值
            for (i in 0..pointCount) {
                val t = i.toFloat() / pointCount
                val interpolatedWorldX = worldStartX + t * (worldEndX - worldStartX)
                val interpolatedWorldY = worldStartY + t * (worldEndY - worldStartY)

                // 直接生成世界坐标点，不再需要复杂的范围检查和归一化
                onPoint(BrushVertex.obtain(interpolatedWorldX, interpolatedWorldY, brushSize, glColor.r, glColor.g, glColor.b))
            }
        }

    }

    fun generatePoints2(startX: Float, startY: Float, endX: Float, endY: Float, onPoint: (BrushPoint) -> Unit) {
        // 1. 将输入的所有View坐标转换为World坐标
        val worldStart = camera.screenToWorld(startX, startY)
        val worldEnd = camera.screenToWorld(endX, endY)

        val worldStartX = worldStart[0]
        val worldStartY = worldStart[1]
        val worldEndX = worldEnd[0]
        val worldEndY = worldEnd[1]

        // 2. 在World坐标系下计算两点间的距离
        val worldDistance = distance(worldStartX, worldStartY, worldEndX, worldEndY)

        // 3. 计算需要插入的点的数量
        // 关键点：我们想保持“屏幕上”的视觉密度恒定。
        // 一个单位的世界距离，在屏幕上会显示为 (1.0 * camera.scale) 的长度。
        // 所以，两点在屏幕上显示的像素距离约等于 (worldDistance * camera.scale * (viewWidth/2)) (粗略估算)
        // 我们需要计算出在世界坐标系下，对应于 `screenSpacePixelInterval` 的距离是多少。
        // 一个NDC单位长度大约是 viewWidth/2 像素。
        val worldSpaceInterval = config.pixInterval * (2f / viewWidth)

        // 因此，需要的点数量就是世界距离除以世界坐标下的点间距
        val pointCount = (worldDistance / worldSpaceInterval).toInt()

        if (pointCount <= 1) {
            // 不需要插入时，使用起始点
            onPoint(BrushPoint(worldStartX, worldStartY))
        } else if (pointCount <= 2) {
            // 插入起始点
            onPoint(BrushPoint(worldStartX, worldStartY))
            // 插入结束点
            onPoint(BrushPoint(worldEndX, worldEndY))
        } else {
            // 4. 在World坐标系下进行插值
            for (i in 0..pointCount) {
                val t = i.toFloat() / pointCount
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
        val scaleStartX = startX * camera.scale
        val scaleEndX = endX * camera.scale
        val scaleStartY = startY * camera.scale
        val scaleEndY = endY * camera.scale
        val pointCount = (distance(scaleStartX, scaleStartY, scaleEndX, scaleEndY) / config.pixInterval / camera.scale).toInt()

        if (pointCount <= 1) {
            val point = createBrushPointInRange(startX, startY, viewWidth, viewHeight, minR, maxR)
            if (point != null) {
                onPoint(point)
            }
        } else if (pointCount <= 2) {
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
                val t = i * 1.0f / pointCount
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
}