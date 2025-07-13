package com.zipper.gl.base.math

class Vector3 {

    var x: Float = 0f
        private set
    var y: Float = 0f
        private set
    var z: Float = 0f
        private set

    constructor(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(v: Vector3) {
        x = v.x
        y = v.y
        z = v.z
    }



}