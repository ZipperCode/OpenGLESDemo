package com.zipper.gl_vector

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.scale
import java.util.LinkedList
import java.util.Queue
import kotlin.math.min


object RegionCalculator {

    init {
        System.loadLibrary("nativelib")
    }

    const val NOT_VISITED: Int = -1
    const val LINE_COLOR_THRESHOLD: Int = 10 // 假设线稿颜色RBG值低于此阈值

    external fun calculateRegions(lineArtBitmap: Bitmap): Bitmap

    fun IntArray.resetX() {
        this[0] = 0
        this[1] = 0
        this[2] = 1
        this[3] = -1
    }
    fun IntArray.resetY() {
        this[0] = 1
        this[1] = -1
        this[2] = 0
        this[3] = 0
    }

    /**
     * 从线稿Bitmap中计算并返回一个区域ID Bitmap (蒙版图)。
     * 每个像素的RGB值将代表其区域ID。
     * @param lineArtBitmap 原始线稿Bitmap
     * @return 包含区域ID的Bitmap (蒙版图)
     */
    fun calculateRegions2(lineArtBitmap: Bitmap): Bitmap {
        val bitmap = lineArtBitmap.scale(1024, 1024, true)
        val time = System.currentTimeMillis()
        Log.d("BAAA", "开始计算")
        val width = bitmap.getWidth()
        val height = bitmap.getHeight()

        val regionIds = Array<IntArray>(width) { IntArray(height) }
        for (i in 0 until width) {
            for (j in 0 until height) {
                regionIds[i][j] = NOT_VISITED
            }
        }

        var currentRegionId = 0
        val queue: Queue<Point> = LinkedList<Point>()
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width , 0, 0, width, height)
        // 检查相邻像素 (上、下、左、右)
        val dx = intArrayOf(0, 0, 1, -1)
        val dy = intArrayOf(1, -1, 0, 0)
        for (i in 0 until pixels.size) {
            val x = i % width
            val y = i / width
            val pixel = pixels[i]
            if (isLineColor(pixel)) {
                regionIds[x][y] = NOT_VISITED
                continue
            }
            if (regionIds[x][y] != NOT_VISITED) {
                continue
            }
            queue.add(Point(x, y))
            regionIds[x][y] = currentRegionId
            while (queue.isNotEmpty()) {
                val p = queue.poll() ?: continue
                dx.resetX()
                dy.resetY()

                for (i in 0..3) {
                    val nx = p.x + dx[i]
                    val ny = p.y + dy[i]

                    if (nx >= 0 && nx < width && ny >= 0 && ny < height && regionIds[nx][ny] == NOT_VISITED) { // 未被访问过
                        val neighborPixel = pixels[ny *width + nx]
                        // 如果相邻像素不是线条，则属于当前区域
                        if (!isLineColor(neighborPixel)) {
                            regionIds[nx][ny] = currentRegionId
                            queue.add(Point(nx, ny))
                        }
                    }
                }
            }
            currentRegionId ++
        }

        Log.d("BAAA", "区域计算完成 = ${System.currentTimeMillis() - time}")

        // 将区域ID数组转换为Bitmap

        val maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        maskBitmap.getPixels(pixels, 0, width , 0, 0, width, height)

        for (i in pixels.indices) {
            val x = i % width
            val y = i / width
            val id = regionIds[x][y]
            if (id != NOT_VISITED) {
                // 将区域ID映射到颜色。这里简单地将ID作为灰度值
                // 注意：如果区域ID非常多，需要更复杂的颜色映射方案
                val colorValue = min(id.toDouble(), 255.0).toInt() // 限制在255以内
                pixels[i] = Color.rgb(colorValue, colorValue, colorValue)
            } else {
                // 线条或未识别区域，可以设置为黑色或白色
                pixels[i] = Color.RED
            }
        }

        Log.d("BAAA", "区域2计算完成 = ${System.currentTimeMillis() - time}")
        return maskBitmap
    }

    // 判断像素是否是线条颜色
    private fun isLineColor(pixel: Int): Boolean {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        // 如果RBG值都很小，则认为是线条
        return r < LINE_COLOR_THRESHOLD && g < LINE_COLOR_THRESHOLD && b < LINE_COLOR_THRESHOLD
    }

    // 辅助类用于存储像素坐标
    class Point internal constructor(var x: Int, var y: Int)
}