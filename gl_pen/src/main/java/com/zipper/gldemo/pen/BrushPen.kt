package com.zipper.gldemo.pen

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.util.Log
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

    private val brushTexture = GLTexture()

    private var lastProcessGlX = 0f
    private var lastProcessGlY = 0f

    private var accumulatedDistance = 0f

    fun initialize() {
        brushTexture.upload(BitmapFactory.decodeStream(context.assets.open(config.brushPath)))
    }

    fun getBrushTexture(): GLTexture = brushTexture

    /**
     * @param curX 屏幕坐标点
     * @param curY 屏幕坐标点
     * @param isFirstDown 是否是第一次按下，表示Down的情况
     */
    fun onScroll(curX: Float, curY: Float, isFirstDown: Boolean, onPoint: (BrushPoint) -> Unit) {
        if (isFirstDown) {
            Log.d("BrushPen", "onScroll: isFirstDown")
            val wordVertex = camera.screenToWorld(curX, curY)
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

        val space = config.brushSize * (1f - camera.renderRatio * camera.viewportRatio)
        val size = config.pixInterval  / 1080f
        Log.d("BAAA", "accumulatedDistance: $accumulatedDistance, size: $size space = $space")

        val spacingNdc = size

        while (accumulatedDistance >= spacingNdc) {
            // 已处理的点到当前点之间的距离
            val remainingPathLengthToCurrent = hypot(worldCurX - lastProcessGlX, worldCurY - lastProcessGlY)
            if (remainingPathLengthToCurrent < spacingNdc) {
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

}