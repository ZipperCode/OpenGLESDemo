package com.zipper.opengl

import android.content.Context
import android.graphics.Matrix

interface IView {

    fun requireContext(): Context

    fun viewWidth(): Int

    fun viewHeight(): Int

    fun renderWidth(): Int

    fun renderHeight(): Int

    fun requestRender(graphicsMatrix: FloatArray)

    fun startAnimation(runnable: Runnable)

    fun cancelAnimation(runnable: Runnable)
}