package com.zipper.gl_vector

import com.zipper.gl_vector.math.Matrix4
import com.zipper.gl_vector.math.Quaternion
import com.zipper.gl_vector.math.Vector3
import kotlin.math.max
import kotlin.math.min

/**
 * 正交投影相机
 */
class OrthographicCamera {
    /**
     * 相机
     * 在世界坐标系中的位置
     */
    private val position = Vector3(0f, 0f, 1f)

    /**
     * 相机查看的目标点 默认 0 0 0 世界坐标中心点
     */
    private val lookAt = Vector3()

    /**
     * 相机向上的方向向量(通常是Y轴)
     */
    private val up = Vector3(0f, 1f, 0f)

    /**
     * 投影矩阵
     */
    private val project = Matrix4()

    /**
     * 相机视图矩阵
     * 将世界坐标系转换到相机空间
     */
    private val view = Matrix4()

    /**
     * 模型矩阵
     * 模型矩阵，用于控制模型在空间中的位置、旋转、缩放
     */
    private val model = Matrix4()

    /**
     * mvp矩阵
     */
    private val mvp = Matrix4()

    private val tmpMatrix = Matrix4()

    val tempVec = Quaternion()
    val tempVec2 = Quaternion()

    /**
     * 齐次坐标，变换之后的最终顶点坐标
     */
    private val finalTopLeftVec = Quaternion()
    private var finalBottomRightVec = Quaternion()

    private val near = 0f
    private val far = 1f

    private var scale = 1f
    private var glTranslateX = 0f
    private var glTranslateY = 0f

    private var renderWidth = 2048
    private var renderHeight = 2048
    val renderRatio get() = renderWidth * 1f / renderHeight
    private var viewportWidth = 1920
    private var viewportHeight = 1080
    val viewportRatio get() = viewportWidth * 1f / viewportHeight

    /**
     * 平板或者长图时滚动速度
     * 渲染高度比宽度大的情况，移动速度为2倍，否则4倍
     */
    val scrollSpeed: Float get() = if (renderRatio <= 1f) 2f else 4f

    /**
     * 更新视口大小
     */
    fun updateViewport(width: Int, height: Int) {
        this.viewportWidth = width
        this.viewportHeight = height
        updateProjectionMatrix()
        updateViewMatrix()
    }

    /**
     * 缩放
     * @param scale 缩放值
     * @param focusX 相对屏幕缩放中心点X
     * @param focusY 相对屏幕缩放中心点Y
     */
    fun onScale(scale: Float, focusX: Float, focusY: Float) {
        // view坐标转化为GL坐标
        val glViewFocusX = (focusX / viewportWidth).normalizeX()
        val glViewFocusY = (focusY / viewportHeight).normalizeY()
        // 确定模型边界
        val topLeftVec = tempVec.set(-1f, 1f, 0f, 1f)
        val bottomRightVec = tempVec2.set(1f, -1f, 0f, 1f)
        // 模型变换
        val tmpMatrix = tmpMatrix
            .reset()
            .scale(scale, scale, scale)
            .translate(glTranslateX, glTranslateY, 0f)
            .multiplyV(topLeftVec)
            .multiplyV(bottomRightVec)

        // 缩放中心点在模型缩放后的位置
        val glModelCenterX = ((glViewFocusX - topLeftVec.x) / (bottomRightVec.x - topLeftVec.x)).normalizeX()
        val glModelCenterY = ((glViewFocusY - topLeftVec.y) / (bottomRightVec.y - topLeftVec.y)).normalizeY()

        // 确定模型缩放中线点的GL坐标
        val focusVec = tempVec.set(glModelCenterX, glModelCenterY, 0f, 1f)
        tmpMatrix.multiplyV(focusVec)

        // 实际偏移的大小
        this.glTranslateX -= (focusVec.x - glViewFocusX) / scale
        this.glTranslateY -= (focusVec.y - glViewFocusY) / scale

        this.scale = scale
        updateModelMatrix()
    }


    /**
     * 缩放
     * @param scale 缩放值
     * @param focusX 相对屏幕缩放中心点X
     * @param focusY 相对屏幕缩放中心点Y
     */
    fun onScaleAnim(scale: Float, focusX: Float, focusY: Float) {
        val glFocusX = (focusX / viewportWidth).normalizeX()
        val glFocusY = (focusY / viewportHeight).normalizeY()

        this.glTranslateX = glTranslateX + (glFocusX - glTranslateX) * scale
        this.glTranslateY = glTranslateY + (glFocusY - glTranslateY) * scale

        this.scale = scale

        clampTranslate()

        updateModelMatrix()
    }

    /**
     * 平移
     * @param dx 相对屏幕坐标
     * @param dy 相对屏幕坐标
     */
    fun onTranslate(dx: Float, dy: Float) {
        val offsetX = (-dx / viewportWidth) * (viewportWidth * 1f / viewportHeight) * renderRatio
        val offsetY = dy / viewportHeight
        this.glTranslateX += (offsetX * scrollSpeed) / scale
        this.glTranslateY += (offsetY * scrollSpeed) / scale
        clampTranslate()
        updateModelMatrix()
    }

    /**
     * 刷新vmp并返回
     */
    fun update(): FloatArray {
        return mvp.reset()
            .multiplyMM(view, model)
            .multiplyMM(project, mvp)
            .multiplyVec(finalTopLeftVec.set(-1f, 1f, 0f, 1f).values())
            .multiplyVec(finalBottomRightVec.set(1f, -1f, 0f, 1f).values())
            .values()
    }

    /**
     * 获取mvp矩阵
     */
    fun getMvpMatrix(): FloatArray {
        return mvp.values()
    }

    /**
     * 判断屏幕坐标是否在模型内，并取出相对于模型坐标
     * @param x 屏幕坐标x
     * @param y 屏幕坐标y
     * @return 相对于模型的坐标
     */
    fun getModelOffset(x: Float, y: Float): FloatArray? {
        val glX = (x / viewportWidth).normalizeX()
        val glY = (y / viewportHeight).normalizeY()

        if (glX > finalBottomRightVec.x || glX < finalTopLeftVec.x || glY > finalTopLeftVec.y || glY < finalBottomRightVec.y) {
            return null
        }
        val modelX = (glX - finalTopLeftVec.x) / (finalBottomRightVec.x - finalTopLeftVec.x)
        val modelY = (glY - finalTopLeftVec.y) / (finalTopLeftVec.y - finalBottomRightVec.y)
        return floatArrayOf(modelX, modelY)
    }

    private fun updateProjectionMatrix() {
        val ratio = this.viewportWidth * 1f / this.viewportHeight
        val renderRatio = this.renderWidth * 1f / this.renderHeight
        var left = -1f
        var right = 1f
        var top = 1f
        var bottom = -1f
        if (ratio > 1f) {
            if (renderRatio > ratio) {
                left = -renderRatio / ratio
                right = renderRatio / ratio
            } else {
                left = -ratio / renderRatio
                right = ratio / renderRatio
            }
        } else {
            if (renderRatio > ratio) {
                top = renderRatio / ratio
                bottom = -renderRatio / ratio
            } else {
                left = -ratio / renderRatio
                right = ratio / renderRatio
            }
        }
        project.orthographic(left, right, bottom, top, near, far)
    }

    private fun updateViewMatrix() {
        view.setLookAt(position, lookAt, up)
    }

    private fun updateModelMatrix() {
        model
            .reset()
            .scale(scale, scale, scale)
            .translate(glTranslateX, glTranslateY, 0f)

    }

    private fun clampTranslate() {
        this.glTranslateX = max(-1f, min(this.glTranslateX, 1f))
        this.glTranslateY = max(-1f, min(this.glTranslateY, 1f))
    }

    private fun Float.normalizeX(): Float {

        return (this - 0.5f) * 2f
    }

    private fun Float.normalizeY(): Float {
        return (-this + 0.5f) * 2f
    }
}