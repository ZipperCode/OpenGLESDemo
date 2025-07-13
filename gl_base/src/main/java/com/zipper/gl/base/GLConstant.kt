package com.zipper.gl.base

/**
 *
 * @author  zhangzhipeng
 * @date    2025/7/4
 */
object GLConstant {
    val VERTEX = floatArrayOf(
        -1.0f, 1.0f, // 左上角,
        -1.0f, -1.0f, // 左下角,
        1.0f, 1.0f, // 右上角,
        1.0f, -1.0f // 右下角,
    )

    val TEXTURE = floatArrayOf(
        0.0f, 0.0f, // 左上角,
        0.0f, 1.0f, // 左下角,
        1.0f, 0.0f, // 右上角,
        1.0f, 1.0f // 右下角,
    )

    val FRAME_BUFFER_TEXTURE = floatArrayOf(
        0.0f, 1.0f, // 左上角 → 左下角
        0.0f, 0.0f, // 左下角 → 左上角
        1.0f, 1.0f, // 右上角 → 右下角
        1.0f, 0.0f  // 右下角 → 右上角
    )

    val TOP_LEFT_VEC = floatArrayOf(-1.0f, 1.0f, 0.0f, 1.0f)
    val BOT_RIGHT_VEC = floatArrayOf(1.0f, -1.0f, 0.0f, 1.0f)

    val identityMatrix = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f)

}