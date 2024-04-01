package com.zipper.opengl.bean

import android.graphics.Color

/**
 * @author zhangzhipeng
 * @date 2024/3/28
 */
class PositionItem {
    var areaColor: IntArray? = null

    var _areaColorARGB = -1

    var groupId = 0

    /* renamed from: h */
    var h = 0
    var identify = 0
    var lx = 0
    var ly = 0
    var originColor: IntArray? = null
    var outRadius = 0.0
    var outX = 0
    var outY = 0
    var radius = 0.0
    var rx = 0
    var ry = 0
    var w = 0
    var x = 0
    var y = 0
    fun getAreaColorARGB(): Int {
        if (_areaColorARGB == -1 && areaColor != null) {
            _areaColorARGB = Color.argb(areaColor!![3], areaColor!![0], areaColor!![1], areaColor!![2])
        }
        return _areaColorARGB
    }

    fun setAreaColorARGB(i2: Int) {
        _areaColorARGB = i2
    }
}