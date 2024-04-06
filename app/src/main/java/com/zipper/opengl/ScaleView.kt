package com.zipper.opengl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import com.zipper.opengl.utils.AssetsUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 *
 * @author zhangzhipeng
 * @date 2024/4/2
 */
class ScaleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), IView {

    private val matrix = Matrix()

    private val gestureHandler = GestureHandler(this)

    private val bitmap: Bitmap = AssetsUtil.getAssetsImage(
        context,
        "902684/maskCombine.png"
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureHandler.onTouchEvent(event)
    }

    override fun draw(canvas: Canvas) {
        canvas.setMatrix(matrix)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        super.draw(canvas)
    }

    override fun requireContext(): Context {
        return context
    }

    override fun viewWidth(): Int {
        return width
    }

    override fun viewHeight(): Int {
        return height
    }

    override fun renderWidth(): Int {
        return bitmap.width
    }

    override fun renderHeight(): Int {
        return bitmap.height
    }

    override fun requestRender(graphicsMatrix: FloatArray) {
        matrix.setValues(graphicsMatrix)
        invalidate()
    }

    override fun startAnimation(runnable: Runnable) {
        postOnAnimation(runnable)
    }

    override fun cancelAnimation(runnable: Runnable) {
        removeCallbacks(runnable)
    }
}
