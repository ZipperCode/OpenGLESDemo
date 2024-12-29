package com.zipper.gldemo2.paint

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ViewConfiguration
import com.zipper.gldemo2.OpenGLHelper

/**
 * 画笔类，负责处理绘画的核心逻辑
 *
 * 主要功能：
 * 1. 处理触摸事件（按下、移动、抬起）并生成平滑的笔画路径
 * 2. 使用贝塞尔曲线算法生成平滑的点序列
 * 3. 管理绘画状态和历史记录
 * 4. 将生成的点转换为OpenGL可用的坐标
 *
 * 工作流程：
 * 1. 按下时记录起始点
 * 2. 移动时生成控制点和终点，使用贝塞尔曲线生成平滑点
 * 3. 将点转换为OpenGL坐标并绘制
 * 4. 抬起时保存当前笔画
 *
 * @property context Android上下文，用于获取系统配置
 * @property brushRenderer 笔画渲染器，负责实际的OpenGL绘制
 */
class BrushPen(
    private val context: Context,
    private val brushRenderer: BrushRenderer
) {
    // 贝塞尔曲线的控制点
    private val fromPoint = BrushPoint()    // 起点
    private val toPoint = BrushPoint()      // 终点
    private val controlPoint = BrushPoint() // 控制点

    // 记录点的历史状态
    private val lastPoint = BrushPoint()    // 上一个点
    private val currentPoint = BrushPoint() // 当前点
    private val downPoint = BrushPoint()    // 按下点

    // OpenGL绘制用的点列表
    private val movePoints = ArrayList<Float>()      // 当前移动产生的点
    private var paintPoints = ArrayList<Float>()     // 当前笔画的所有点

    // 笔画历史记录
    private val paintStack = ArrayList<PaintPoints>() // 所有笔画的历史记录
    private var currentPaints: PaintPoints? = null   // 当前正在绘制的笔画

    // 系统配置
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop // 最小移动距离

    // 状态标记
    private var hasMoved = false // 是否发生过移动

    // 笔画属性
    var pointSize = 100f    // 点的大小
    var penColor = Color.RED // 画笔颜色

    /**
     * 处理按下事件
     * 记录起始状态并初始化新的笔画
     *
     * @param point 按下的点
     */
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

    /**
     * 处理移动事件
     * 生成平滑的笔画路径并绘制
     *
     * @param eventPoint 当前触摸点
     * @param viewWidth 视图宽度，用于坐标转换
     * @param viewHeight 视图高度，用于坐标转换
     */
    fun onActionMove(eventPoint: BrushPoint, viewWidth: Int, viewHeight: Int) {
        val distance = fromPoint.distanceTo(eventPoint)
        if (distance < touchSlop) {
            // 移动距离太小，忽略
            return
        }
        hasMoved = true
        currentPoint.set(eventPoint)
        
        // 终点设置为上个点和当前点的加权点
        toPoint.setMultiplySum(lastPoint, currentPoint)
        // 使用上一个点作为控制点
        controlPoint.set(lastPoint)

        // 生成平滑点并转换为OpenGL坐标
        val list = PointHelper.smoothPoints(fromPoint, toPoint, controlPoint, pointSize)
        movePoints.clear()
        list.forEach {
            val points = OpenGLHelper.convertGlVertex(it.x / viewWidth, it.y / viewHeight)
            movePoints.add(points[0])
            movePoints.add(points[1])
        }

        // 绘制点
        if (movePoints.isNotEmpty()) {
            paintPoints.addAll(movePoints)
            brushRenderer.drawPoints(movePoints)
            movePoints.clear()
        }

        // 更新状态
        fromPoint.set(toPoint)
        lastPoint.set(eventPoint)
    }

    /**
     * 处理抬起事件
     * 完成当前笔画并保存到历史记录
     *
     * @param eventPoint 抬起的点
     * @param viewWidth 视图宽度
     * @param viewHeight 视图高度
     */
    fun onActionUp(eventPoint: BrushPoint, viewWidth: Int, viewHeight: Int) {
        if (!hasMoved) {
            // 没有移动，绘制单个点
            val points = OpenGLHelper.convertGlVertex(eventPoint.x / viewWidth, eventPoint.y / viewHeight)
            paintPoints.add(points[0])
            paintPoints.add(points[1])
            brushRenderer.drawPoints(paintPoints)
        }

        // 保存当前笔画到历史记录
        if (currentPaints != null) {
            brushRenderer.saveFrame(paintPoints)
            currentPaints!!.points.addAll(paintPoints)
            paintStack.add(currentPaints!!)
            paintPoints.clear()
            currentPaints = null
        }
    }

    /**
     * 笔画数据类
     * 存储单次笔画的点和颜色信息
     */
    class PaintPoints {
        var points = ArrayList<Float>() // OpenGL坐标点列表
        var color = Color.RED          // 笔画颜色
    }
}