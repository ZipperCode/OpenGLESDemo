package com.zipper.gl_vector.surfaceview

/**
 *
 * @author  zhangzhipeng
 * @date    2025/7/4
 */
interface IRender {
    fun onCreated()

    fun onSizeChanged(width: Int, height: Int)

    fun onRender()
}