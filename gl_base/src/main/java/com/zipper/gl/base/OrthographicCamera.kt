package com.zipper.gl.base

import android.opengl.Matrix
import android.util.Log
import com.zipper.gl.base.math.Matrix4
import com.zipper.gl.base.math.Vector3
import com.zipper.gl.base.math.Vector4
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

    val tempVec = Vector4()
    val tempVec2 = Vector4()

    /**
     * 齐次坐标，变换之后的最终顶点坐标
     */
    private val finalTopLeftVec = Vector4()
    private var finalBottomRightVec = Vector4()

    private val near = 0f
    private val far = 1f

    var scale = 1f
        private set
    private var glTranslateX = 0f
    private var glTranslateY = 0f

    private var modelWidth = 2048
    private var modelHeight = 2048
    val renderRatio get() = modelWidth * 1f / modelHeight
    var viewportWidth = 1920
        private set
    var viewportHeight = 1080
        private set
    val viewportRatio get() = viewportWidth * 1f / viewportHeight

    /**
     * 平板或者长图时滚动速度
     * 渲染高度比宽度大的情况，移动速度为2倍，否则4倍
     */
    val scrollSpeed: Float get() = if (renderRatio <= 1f) 2f else 4f

    var pixelsPerModelUnit = 0.01f

    /**
     * 更新视口大小
     */
    fun updateViewport(width: Int, height: Int) {
        this.viewportWidth = width
        this.viewportHeight = height
        updateProjectionMatrix()
        updateViewMatrix()
        update()
        updateModelUnitSize()
    }

    fun updateRenderSize(width: Int, height: Int) {
        this.modelWidth = width
        this.modelHeight = height
        updateProjectionMatrix()
        updateViewMatrix()
        update()
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
    fun onScroll(dx: Float, dy: Float) {
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
     * 获取屏幕坐标对应的模型坐标
     * 如果超出边界则返回边界值
     */
    fun getModelOffset(x: Float, y: Float): FloatArray {
        val glX = (x / viewportWidth).normalizeX()
        val glY = (y / viewportHeight).normalizeY()

        // 如果超出边界，设置为边界值
        val clampedGlX = glX.coerceIn(finalTopLeftVec.x..finalBottomRightVec.x)
        val clampedGlY = glY.coerceIn(finalBottomRightVec.y..finalTopLeftVec.y)

        val modelX = (clampedGlX - finalTopLeftVec.x) / (finalBottomRightVec.x - finalTopLeftVec.x)
        val modelY = (clampedGlY - finalTopLeftVec.y) / (finalTopLeftVec.y - finalBottomRightVec.y)
        return floatArrayOf(modelX, modelY)
    }

    fun getNdcVertex(x: Float, y: Float, vec: Vector4) {
        val ndcX = (x / viewportWidth).normalizeX()
        val ndcY = (y / viewportHeight).normalizeY()
        val invertedMvpMatrix = FloatArray(16)
        Matrix.invertM(invertedMvpMatrix, 0, mvp.values(), 0)
        val worldCoords = FloatArray(4)
        val ndcCoords = floatArrayOf(ndcX, ndcY, 0f, 1f) // z 和 w 分量
        Matrix.multiplyMV(worldCoords, 0, invertedMvpMatrix, 0, ndcCoords, 0);

//        mvp.multiplyVec(vec.set(ndcCoords[0], glY, 0f, 1f).values())
    }

    fun getNdcVertex(x: Float, y: Float): FloatArray {
        val ndcX = (x / viewportWidth).normalizeX()
        val ndcY = (y / viewportHeight).normalizeY()
        val invertedMvpMatrix = FloatArray(16)
        Matrix.invertM(invertedMvpMatrix, 0, mvp.values(), 0)
        val worldCoords = FloatArray(4)
        val ndcCoords = floatArrayOf(ndcX, ndcY, 0f, 1f) // z 和 w
        Matrix.multiplyMV(worldCoords, 0, invertedMvpMatrix, 0, ndcCoords, 0);
        return ndcCoords
    }

    fun screenToWorld(screenX: Float, screenY: Float): FloatArray {
        // 1. 转换到 NDC 坐标系
        // Y 轴需要翻转，因为屏幕坐标原点在左上，NDC 在中间
        val ndcX: Float = (screenX / viewportWidth) * 2.0f - 1.0f
        val ndcY: Float = 1.0f - (screenY / viewportHeight) * 2.0f

        // 2. 计算 MVP 矩阵及其逆矩阵
        val mvpMatrix = FloatArray(16)
        val invertedMvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, project.values(), 0, model.values(), 0)
        Matrix.invertM(invertedMvpMatrix, 0, mvpMatrix, 0)

        // 3. 将 NDC 坐标乘以逆矩阵，转换到世界坐标
        val worldCoords = FloatArray(4)
        val ndcCoords = floatArrayOf(ndcX, ndcY, 0f, 1f) // z 和 w 分量
        Matrix.multiplyMV(worldCoords, 0, invertedMvpMatrix, 0, ndcCoords, 0)

        // 4. 透视除法
        if (worldCoords[3] != 0f) {
            worldCoords[0] /= worldCoords[3]
            worldCoords[1] /= worldCoords[3]
        }

        return floatArrayOf(worldCoords[0], worldCoords[1])
    }


    fun getMinRange(): FloatArray = finalTopLeftVec.values()
    fun getMaxRange(): FloatArray = finalBottomRightVec.values()

    private fun updateProjectionMatrix() {
        val ratio = this.viewportWidth * 1f / this.viewportHeight
        val renderRatio = this.modelWidth * 1f / this.modelHeight
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

    fun updateModelUnitSize() {
        val p1 = floatArrayOf(0f, 0f, 0f, 1f)
        val p2 = floatArrayOf(1f, 0f, 0f, 1f)

        mvp.multiplyVec(p1)
        mvp.multiplyVec(p2)

        val p1NdcW = p1[3]
        val p2NdcW = p2[3]
        if (p1NdcW == 0f || p2NdcW == 0f) {
            pixelsPerModelUnit = 1f
            return
        }
        val p1ndxX = p1[0] / p1NdcW
        val p1ndxY = p1[1] / p1NdcW
        val p2ndxX = p2[0] / p2NdcW
        val p2ndxY = p2[1] / p2NdcW

        val distance = distance(p1ndxX, p1ndxY, p2ndxX, p2ndxY)
        // Log.i("BAAA", "p1X = $p1ndxX p1Y = $p1ndxY p2X = $p2ndxX p2Y = $p2ndxY d = $distance")

        val p1ScreenX = (p1ndxX + 1) * viewportWidth / 2f
        val p1ScreenY = (1f - p1ndxY) * viewportHeight / 2f
        val p2ScreenX = (p2ndxX + 1) * viewportWidth / 2f
        val p2ScreenY = (1f - p2ndxY) * viewportHeight / 2f

        val screenDistance = distance(p1ScreenX, p1ScreenY, p2ScreenX, p2ScreenY)

        // Log.d("BAAA", "screenDistance = $screenDistance")
        pixelsPerModelUnit = screenDistance
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