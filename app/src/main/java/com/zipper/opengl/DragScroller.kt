package com.zipper.opengl

import android.content.Context
import android.view.ViewConfiguration
import android.view.animation.LinearInterpolator
import android.widget.OverScroller
import android.widget.Scroller

/**
 *
 * @author zhangzhipeng
 * @date 2024/4/3
 */
class DragScroller(context: Context) : OverScroller(context, LinearInterpolator()) {
    init {
        currVelocity
        setFriction(ViewConfiguration.getScrollFriction() * 100)
    }
}
