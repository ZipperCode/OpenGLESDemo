package com.zipper.gldemo2.paint

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

object PointHelper {

    private val pointsPerLengthSum = 10

    private fun isCenter(center: BrushPoint, from: BrushPoint, to: BrushPoint): Boolean {
        val isCenterX = (from.x + to.x) * 0.5f - center.x < 0.001
        val isCenterY = (from.y + to.y) * 0.5f - center.y < 0.001
        return isCenterX && isCenterY
    }

    /**
     * 平滑插值
     */
    fun smoothPoints(from: BrushPoint, to: BrushPoint, control: BrushPoint, pointSize: Float = 15f): List<BrushPoint> {
        // 如果是中心点，忽略
        val p1 = if (isCenter(control, from, to)) from else control

        val ax = from.x - 2 * p1.x + to.x
        val ay = from.y - 2 * p1.y + to.y
        val bx = 2 * p1.x - 2 * from.x
        val by = 2 * p1.y - 2 * from.y

        val a = 4.0 * (ax * ax + ay * ay)
        val b = 4.0 * (ax * bx + ay * by)
        val c = 1.0 * bx * bx + by * by

        // 整条曲线的长度
        val totalLength = lengthT(1.0, a, b, c)
        // 用点的尺寸计算出，单位长度需要多少个点
        val pointsPerLength = pointsPerLengthSum / pointSize
        // 曲线上生成的点数
        val pointCount = max(1, (pointsPerLength * totalLength).toInt())
        val result = ArrayList<BrushPoint>(pointCount)
        for (i in 0 until pointCount) {
            var t = i * 1.0 / pointCount
            val length = t * totalLength
            t = t2(t, length, a, b, c)

            // 根据 t 求出坐标
            val x = (1 - t) * (1 - t) * from.x + 2 * (1 - t) * t * p1.x + t * t * to.x
            val y = (1 - t) * (1 - t) * from.y + 2 * (1 - t) * t * p1.y + t * t * to.y
            result.add(BrushPoint(x.toFloat(), y.toFloat()))
        }

        return result
    }

    /**
     * 计算贝塞尔曲线的弧长
     */
    private fun lengthT(t: Double, a: Double, b: Double, c: Double): Double {
        if (a < 0.00001f) {
            return 0.0
        }
        val temp1 = sqrt(c + t * (b + a * t))
        val temp2 = (2 * a * t * temp1 + b * (temp1 - sqrt(c)));
        val temp3 = ln(abs(b + 2 * sqrt(a) * sqrt(c) + 0.0001))
        val temp4 = ln(abs(b + 2 * a * t + 2 * sqrt(a) * temp1) + 0.0001)
        val temp5 = 2 * sqrt(a) * temp2;
        val temp6 = (b * b - 4 * a * c) * (temp3 - temp4);

        return (temp5 + temp6) / (8 * a.pow(1.5))
    }

    /**
     * 速度公式
     * 速度函数 s(t) = sqrt(A * t^2 + B * t + C)
     * @return 贝塞尔曲线某一点的速度
     */
    private fun speedT(t: Double, a: Double, b: Double, c: Double): Double {
        return sqrt((a * t.pow(2.0) + b * t + c).coerceAtLeast(0.0))
    }

    /**
     * 长度函数反函数，根据 length，求出对应的 t，使用牛顿切线法求解
     * @param t 给出的近似的 t，比如求长度占弧长 0.3 的 t，t 应该是接近 0.3，则传入近似值 0.3
     * @param length 目标弧长，实际长度，非占比
     * @return 结果 t 值
     */
    private fun t(t: Double, length: Double, a: Double, b: Double, c: Double): Double {
        var t1 = t
        var t2: Double
        var lastDouble = 0.0
        while (true) {
            val speed = speedT(t, a, b, c)
            if (speed < 0.0001) {
                t2 = t1
                break
            }
            t2 = t1 - (lengthT(t1, a, b, c) - length) / speed
            if (abs(t1 - t2) < 0.0001) {
                break
            }
            //02-04 23:21:59.644 20094-20094/? E/11111: t1 0.501487692151914 t2 0.4985078813922082

            //Log.e("11111", "t1 $t1 t2 $t2")
            if (lastDouble == t1) {
                break
            }

            lastDouble = t2
            t1 = t2
        }
        return t2
    }
    private fun t2(t: Double, length: Double, a: Double, b: Double, c: Double): Double {
        var t1 = t
        var t2: Double = 0.0
        var lastDouble = 0.0
        var iterations = 0 // 添加迭代计数器
        val maxIterations = 20 // 设置最大迭代次数
        while (iterations < maxIterations) { // 增加迭代次数判断
            val speed = speedT(t, a, b, c)
            if (speed < 0.0001) {
                t2 = t1
                break
            }
            t2 = t1 - (lengthT(t1, a, b, c) - length) / speed
            if (abs(t1 - t2) < 0.0001) {
                break
            }
            if (lastDouble == t1) {
                break
            }
            lastDouble = t2
            t1 = t2
            iterations++ // 增加迭代计数
        }
        // 如果超过最大迭代次数仍然没有收敛，可以添加处理逻辑
        if (iterations >= maxIterations) {
            // 可以选择返回上一次的t值，或者使用其他方法计算t值
            println("Warning: t() did not converge after $maxIterations iterations.")
            return t1
        }
        return t2
    }
}