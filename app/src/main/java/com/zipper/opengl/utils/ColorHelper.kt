package com.zipper.opengl.utils

import android.graphics.Color

/**
 *
 * @author zhangzhipeng
 * @date 2024/4/1
 */
object ColorHelper {

    fun decodeInColorArr(colorStr: String?, colorArr: FloatArray) {
        var parseColor = Color.parseColor(colorStr)
        if (parseColor == -1) {
            parseColor = 0
        }
        if (parseColor == -16777216) {
            colorArr[0] = -0.39215687f
            colorArr[1] = -0.39215687f
            colorArr[2] = -0.39215687f
            colorArr[3] = 1.0f
            return
        }
        colorArr[0] = Color.red(parseColor) / 255.0f
        colorArr[1] = Color.green(parseColor) / 255.0f
        colorArr[2] = Color.blue(parseColor) / 255.0f
        colorArr[3] = Color.alpha(parseColor) / 255.0f
    }

    fun setColorArr(color: Int, colorArr: FloatArray) {
        colorArr[0] = Color.red(color) / 255.0f
        colorArr[1] = Color.green(color) / 255.0f
        colorArr[2] = Color.blue(color) / 255.0f
        colorArr[3] = Color.alpha(color) / 255.0f
    }
}
