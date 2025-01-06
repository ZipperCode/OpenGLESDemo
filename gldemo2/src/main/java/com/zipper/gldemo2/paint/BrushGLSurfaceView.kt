package com.zipper.gldemo2.paint

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import com.zipper.gldemo2.selection.PickColorDrawer
import com.zipper.gldemo2.selection.SelectionDrawer
import com.zipper.gldemo2.selection.SelectionRect

class BrushGLSurfaceView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

    val renderer = BrushRenderer(this)
    private val eventPoint = BrushPoint()
    public enum class Mode {
        Selection,
        PickColor,
        Normal
    }
    private var mode = Mode.Normal
    private val selectionRect = SelectionRect()
    private val pickRect = SelectionRect()
    private val selectionDrawer = SelectionDrawer()
    private val pickColorDrawer = PickColorDrawer()

    var onPickColor = { color: Int -> }

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        // 初始化选区位置为视图中心的200x200矩形
        post {
            val centerX = width / 2f
            val centerY = height / 2f
            selectionRect.initPosition(
                centerX - 100,
                centerY - 100,
                centerX + 100,
                centerY + 100
            )
            pickRect.initPosition(centerX - 10,
                centerY - 10,
                centerX + 10,
                centerY + 10 )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mode == Mode.Selection || mode == Mode.PickColor) {
            // 选区模式下的触摸处理
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (selectionRect.isTouchInside(event.x, event.y)) {
                        selectionRect.startMove(event.x, event.y)
                        invalidate()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    selectionRect.moveRect(event.x, event.y, width.toFloat(), height.toFloat())
                    invalidate()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    selectionRect.endMove()
                    invalidate()
                    if (mode == Mode.PickColor) {
                        getPickContent()?.run {
                            onPickColor.invoke(this)
                        }
                    }
                }
            }
        } else {
            // 绘制模式下的触摸处理
            eventPoint.set(event.x, event.y)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    renderer.onActionDown(eventPoint)
                    requestRender()
                }
                MotionEvent.ACTION_MOVE -> {
                    renderer.onActionMove(eventPoint)
                    requestRender()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    renderer.onActionUp(eventPoint)
                    requestRender()
                }
            }
        }
        return true
    }

    fun reset(initColor: Int = Color.RED) {
        mode = Mode.Normal
        renderer.reset(initColor)
        requestRender()
    }

    /**
     * 设置选区模式
     */
    fun setSelectionMode(enabled: Boolean) {
        mode = Mode.Selection
        invalidate()
    }

    fun setMode(mode: Mode) {
        this.mode = mode
        invalidate()
    }

    /**
     * 获取选区内容
     */
    fun getSelectionContent(): Bitmap? {
        val rect = selectionRect.getRect()
        renderer.requestCapture(rect)
        // 等待渲染完成
        requestRender()
        return renderer.getCaptureBitmap()
    }

    fun getPickContent(): Int? {
        val rect = selectionRect.getRect()
        val centerX = rect.centerX()
        val centerY = rect.centerY()
        val pickRect = RectF(centerX - 5, centerY - 5, centerX + 5, centerY + 5)
        renderer.requestCapture(pickRect)
        requestRender()
        Thread.sleep(100)
        return renderer.getCaptureBitmap()?.run {
            getPixel(width / 2, height / 2)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        // 在选区模式下绘制选区
        if (mode == Mode.Selection) {
            selectionDrawer.drawSelection(canvas, selectionRect.getRect())
        } else if (mode == Mode.PickColor){
            pickColorDrawer.drawSelection(canvas, selectionRect.getRect())
        }
    }
}