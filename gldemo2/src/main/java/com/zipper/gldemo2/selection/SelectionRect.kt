package com.zipper.gldemo2.selection

import android.graphics.RectF

/**
 * 选区矩形
 */
class SelectionRect {
    // 选区矩形
    private val rect = RectF()
    // 是否正在移动选区
    private var isMoving = false
    // 移动开始时的触摸点
    private var startX = 0f
    private var startY = 0f
    // 移动开始时的矩形位置
    private var startLeft = 0f
    private var startTop = 0f

    /**
     * 初始化选区位置
     */
    fun initPosition(left: Float, top: Float, right: Float, bottom: Float) {
        rect.set(left, top, right, bottom)
    }

    /**
     * 判断触摸点是否在选区内
     */
    fun isTouchInside(x: Float, y: Float): Boolean {
        return rect.contains(x, y)
    }

    /**
     * 开始移动选区
     */
    fun startMove(x: Float, y: Float) {
        isMoving = true
        startX = x
        startY = y
        startLeft = rect.left
        startTop = rect.top
    }

    /**
     * 移动选区
     */
    fun moveRect(x: Float, y: Float, viewWidth: Float, viewHeight: Float) {
        if (!isMoving) return

        val dx = x - startX
        val dy = y - startY

        // 计算新的位置
        var newLeft = startLeft + dx
        var newTop = startTop + dy

        // 确保选区不会超出视图边界
        if (newLeft < 0) newLeft = 0f
        if (newTop < 0) newTop = 0f
        if (newLeft + rect.width() > viewWidth) newLeft = viewWidth - rect.width()
        if (newTop + rect.height() > viewHeight) newTop = viewHeight - rect.height()

        rect.offsetTo(newLeft, newTop)
    }

    /**
     * 结束移动选区
     */
    fun endMove() {
        isMoving = false
    }

    /**
     * 获取选区矩形
     */
    fun getRect(): RectF {
        return RectF(rect)
    }
}
