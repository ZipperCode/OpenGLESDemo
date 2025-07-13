package com.zipper.gldemo.pen

import android.content.Context
import com.zipper.gl.base.GLTexture
import com.zipper.gl.base.OrthographicCamera

/**
 * 笔刷
 */
class BrushPen(
    private val context: Context,
    private val config: BrushConfig,
    private val camera: OrthographicCamera
) {
    /**
     * 当前笔刷大小
     */
    var brushSize = 20f

    private val

    fun onScroll(startX: Float, startY: Float, endX: Float, endY: Float) {

    }
}