package com.zipper.gldemo.pen

object BrushManager {

    private val defaultBrushMap = mapOf<String, BrushConfig>(
        "Brush1" to BrushConfig(
            name = "Brush1",
            brushPath = "eraser.png",
            brushSize = 40f,
            pixInterval = 2,
        ),
        "Brush2" to BrushConfig(
            name = "Brush2",
            brushPath = "brush.png",
            brushSize = 40f,
            pixInterval = 4,
        ),
        "Brush3" to BrushConfig(
            name = "Brush3",
            brushPath = "seal_clover.png",
            brushSize = 40f,
            pixInterval = 40,
        ),
    )

    fun getBrushConfigs(name: String): BrushConfig? {
        return defaultBrushMap[name]
    }
}