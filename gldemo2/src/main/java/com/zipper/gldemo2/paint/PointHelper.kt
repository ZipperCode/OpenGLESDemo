package com.zipper.gldemo2.paint

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 笔画点生成辅助类
 * 
 * 主要功能：
 * 1. 使用二次贝塞尔曲线生成平滑的笔画路径
 * 2. 处理控制点和采样点的生成
 * 3. 确保生成的点之间距离合适，避免笔画断裂或不自然
 */
object PointHelper {

    /**
     * 每单位长度的基准点数量
     * 这个值会和点大小（pointSize）一起决定实际生成的点密度
     * 较大的值会产生更密集的点，笔画更平滑但性能消耗更大
     */
    private val pointsPerLengthSum = 15

    /**
     * 判断一个点是否为两点的中点
     * 用于处理控制点的特殊情况，避免曲线出现不必要的弯曲
     * 
     * @param center 待判断的点
     * @param from 起点
     * @param to 终点
     * @return 如果center是from和to的中点则返回true
     */
    private fun isCenter(center: BrushPoint, from: BrushPoint, to: BrushPoint): Boolean {
        val isCenterX = (from.x + to.x) * 0.5f - center.x < 0.001
        val isCenterY = (from.y + to.y) * 0.5f - center.y < 0.001
        return isCenterX && isCenterY
    }

    /**
     * 生成平滑的笔画路径点
     * 使用二次贝塞尔曲线算法，根据起点、终点和控制点生成一系列平滑的路径点
     * 
     * 算法原理：
     * 1. 二次贝塞尔曲线公式：B(t) = (1-t)²P₀ + 2(1-t)tP₁ + t²P₂
     *    其中 P₀是起点，P₁是控制点，P₂是终点，t∈[0,1]
     * 2. 曲线长度通过积分计算：L = ∫₀¹ |B'(t)|dt
     * 3. 根据曲线长度和点大小计算采样点数量
     * 4. 使用参数方程生成实际的点坐标
     * 
     * @param from 起点
     * @param to 终点
     * @param control 控制点
     * @param pointSize 点的大小，影响生成点的密度
     * @return 生成的路径点列表
     */
    fun smoothPoints(from: BrushPoint, to: BrushPoint, control: BrushPoint, pointSize: Float = 15f): List<BrushPoint> {
        // 处理控制点，如果控制点是起点和终点的中点，则使用起点作为控制点
        // 这样可以避免不必要的曲线弯曲
        val p1 = if (isCenter(control, from, to)) from else control

        // 计算贝塞尔曲线的系数
        // 这些系数用于计算曲线的参数方程
        val ax = from.x - 2 * p1.x + to.x  // 二次项系数
        val ay = from.y - 2 * p1.y + to.y
        val bx = 2 * p1.x - 2 * from.x     // 一次项系数
        val by = 2 * p1.y - 2 * from.y

        // 计算速度函数的系数
        // 速度函数用于计算曲线的弧长
        val a = 4.0 * (ax * ax + ay * ay)  // 二次项
        val b = 4.0 * (ax * bx + ay * by)  // 一次项
        val c = 1.0 * bx * bx + by * by    // 常数项

        // 计算整条曲线的长度
        val totalLength = lengthT(1.0, a, b, c)
        
        // 计算需要生成的点数
        // 点数 = 单位长度点数 / 点大小 * 曲线长度
        val pointsPerLength = pointsPerLengthSum / pointSize
        val pointCount = max(1, (pointsPerLength * totalLength).toInt())
        
        val result = ArrayList<BrushPoint>(pointCount)
        var lastPoint: BrushPoint? = null
        val maxDistance = pointSize * 1.5f // 控制相邻点之间的最大距离

        // 生成并过滤点
        for (i in 0 until pointCount) {
            // 计算参数t，范围[0,1]
            var t = i * 1.0 / pointCount
            val length = t * totalLength
            // 根据弧长参数化调整t值，使点的分布更均匀
            t = t2(t, length, a, b, c)

            // 使用贝塞尔曲线公式计算点的坐标
            val x = (1 - t) * (1 - t) * from.x + 2 * (1 - t) * t * p1.x + t * t * to.x
            val y = (1 - t) * (1 - t) * from.y + 2 * (1 - t) * t * p1.y + t * t * to.y
            val point = BrushPoint(x.toFloat(), y.toFloat())

            // 处理第一个点
            if (lastPoint == null) {
                result.add(point)
                lastPoint = point
                continue
            }

            // 检查与上一个点的距离
            // 如果距离过大，则跳过该点，避免笔画出现断裂
            val distance = sqrt(
                (point.x - lastPoint.x) * (point.x - lastPoint.x) +
                (point.y - lastPoint.y) * (point.y - lastPoint.y)
            )

            if (distance <= maxDistance) {
                result.add(point)
                lastPoint = point
            }
        }

        // 处理特殊情况
        if (result.size < 2) {
            // 如果生成的点太少，至少确保有起点和终点
            result.clear()
            result.add(BrushPoint(from.x, from.y))
            result.add(BrushPoint(to.x, to.y))
        } else if (result.last() != lastPoint) {
            // 如果最后一个点被过滤掉了，添加终点
            // 确保曲线能够连接到终点
            result.add(BrushPoint(to.x, to.y))
        }

        return result
    }

    /**
     * 计算贝塞尔曲线某一点的弧长
     * 使用解析方法计算从起点到参数t处的弧长
     * 
     * @param t 曲线参数，范围[0,1]
     * @param a 速度函数二次项系数
     * @param b 速度函数一次项系数
     * @param c 速度函数常数项
     * @return 从起点到参数t处的弧长
     */
    private fun lengthT(t: Double, a: Double, b: Double, c: Double): Double {
        if (a < 0.00001f) {
            return 0.0
        }
        val temp1 = sqrt(c + t * (b + a * t))
        val temp2 = (2 * a * t * temp1 + b * (temp1 - sqrt(c)))
        val temp3 = ln(abs(b + 2 * sqrt(a) * sqrt(c) + 0.0001))
        val temp4 = ln(abs(b + 2 * a * t + 2 * sqrt(a) * temp1) + 0.0001)
        val temp5 = 2 * sqrt(a) * temp2
        val temp6 = (b * b - 4 * a * c) * (temp3 - temp4)

        return (temp5 + temp6) / (8 * a.pow(1.5))
    }

    /**
     * 计算给定弧长对应的参数t值
     * 使用牛顿迭代法求解非线性方程
     * 
     * @param t 初始猜测值
     * @param length 目标弧长
     * @param a 速度函数二次项系数
     * @param b 速度函数一次项系数
     * @param c 速度函数常数项
     * @return 对应目标弧长的参数t值
     */
    private fun t2(t: Double, length: Double, a: Double, b: Double, c: Double): Double {
        var t1 = t
        var lastDouble = 0.0
        while (true) {
            // 计算当前点的速度
            val speed = sqrt((a * t1 * t1 + b * t1 + c).coerceAtLeast(0.0))
            if (speed < 0.0001) {
                break
            }
            
            // 牛顿迭代公式：t(n+1) = t(n) - f(t)/f'(t)
            // 其中f(t) = lengthT(t) - length
            val t2 = t1 - (lengthT(t1, a, b, c) - length) / speed
            
            // 收敛判断
            if (abs(t1 - t2) < 0.0001 || lastDouble == t1) {
                break
            }
            lastDouble = t1
            t1 = t2
        }
        return t1
    }
}