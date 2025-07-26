package com.zipper.gldemo.pen

class BrushVertex {

    var vertexX: Float = 0f

    var vertexY: Float = 0f

    var brushSize: Float = 0f

    var r: Float = 0f

    var g: Float = 0f

    var b: Float = 0f

    var angle: Float = 0f

    private var inUsed = true

    private var next: BrushVertex? = null


    fun recycle() {
        vertexX = 0f
        vertexY = 0f
        brushSize = 0f
        r = 0f
        g = 0f
        b = 0f
        inUsed = false

        synchronized(sPoolLock) {
            next = sPool
            sPool = this
            sPoolSize++
        }
    }


    companion object {
        private val sPoolLock = Any()

        private var sPool: BrushVertex? = null

        private var sPoolSize = 0


        fun obtain(): BrushVertex {
            synchronized(sPoolLock) {
                if (sPool != null) {
                    val node = sPool!!
                    sPool = node.next
                    node.next = null
                    node.inUsed = true
                    sPoolSize--
                    return node
                }
            }
            return BrushVertex()
        }

        fun obtain(vertexX: Float, vertexY: Float, angle: Float, brushSize: Float, r: Float, g: Float, b: Float): BrushVertex {
            return obtain().apply {
                this.vertexX = vertexX
                this.vertexY = vertexY
                this.brushSize = brushSize
                this.r = r
                this.g = g
                this.b = b
                this.angle = angle
            }
        }
    }
}