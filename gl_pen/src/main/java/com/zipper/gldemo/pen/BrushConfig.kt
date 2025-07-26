package com.zipper.gldemo.pen

/**
 * 笔刷配置
 */
data class BrushConfig(
    val name: String,
    val brushPath: String,
    val brushSize: Float,
    val pixInterval: Int,
)