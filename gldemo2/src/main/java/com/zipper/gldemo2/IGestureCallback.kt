package com.zipper.gldemo2

import android.content.Context

/**
 *
 * @author  zhangzhipeng
 * @date    2024/4/8
 */
interface IGestureCallback {

    fun requireContext(): Context


    fun onSingleTapUp(x: Float, y: Float)

    /**
     * 滚动时
     */
    fun onScroll(distanceX: Float, distanceY: Float)

}