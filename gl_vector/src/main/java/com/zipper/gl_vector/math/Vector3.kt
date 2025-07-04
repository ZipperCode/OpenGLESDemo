package com.zipper.gl_vector.math

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector3

        if (x.toIntBits() != other.x.toIntBits()) return false
        if (y.toIntBits() != other.y.toIntBits()) return false
        if (z.toIntBits() != other.z.toIntBits()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.toIntBits().hashCode()
        result = 31 * result + y.toIntBits().hashCode()
        result = 31 * result + z.toIntBits().hashCode()
        return result
    }

}