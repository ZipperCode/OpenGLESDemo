package com.zipper.gldraw.math

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 *
 * @author  zhangzhipeng
 * @date    2025/6/24
 */
class Quaternion(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    var w: Float = 1f
) {

    fun length(): Float {
        return sqrt(x * x + y * y + z * z + w * w)
    }

    fun setEulerAngles(yaw: Float, pitch: Float, roll: Float) = apply {
        val halfRoll = roll * 0.5f
        val shr = sin(halfRoll)
        val chr = cos(halfRoll)
        val halfPitch = pitch * 0.5f
        val shp = sin(halfPitch)
        val chp = cos(halfPitch)
        val halfYaw = yaw * 0.5f
        val shy = sin(halfYaw)
        val chy = cos(halfYaw)

        x = (shr * chp * chy) + (chr * shp * shy)
        y = (chr * shp * chy) - (shr * chp * shy)
        z = (chr * chp * shy) - (shr * shp * chy)
        w = (chr * chp * chy) + (shr * shp * shy)
    }
}