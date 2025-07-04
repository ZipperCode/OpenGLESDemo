package com.zipper.gldraw.math

/**
 *
 * @author  zhangzhipeng
 * @date    2025/6/24
 */
class Matrix4 {

    companion object {
        val IDENTITY = floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )

        /**
         * XX: 通常是用于缩放的无座的X分量，当在y和/或z轴上旋转时，角度的余弦。在Vector3乘法上，此值乘以源X组件并添加到目标X组件中。
         */
        const val M00 = 0

        /**
         * XY: 通常在Z轴上旋转时角度的负正弦。在vector3乘法中，此值乘以源y组件并添加到目标x组件中。
         */
        const val M01 = 4

        /**
         * XZ: 通常在y轴上旋转时角度的正弦。在vector3乘法上，此值乘以源z组件并添加到目标x组件中。
         */
        const val M02 = 8

        /**
         * XW：通常是X组件的转换。在Vector3乘法上，此值将添加到目标X组件中。
         */
        const val M03 = 12

        /**
         * YX：通常在Z轴上旋转时角度的正弦。在vector3乘法上，此值乘以源x组件并添加到目标y组件中。
         */
        const val M10 = 1

        /**
         * YY：通常是用于缩放的Y分量，当在X和/或Z轴上旋转时，角度的余弦。在vector3乘法中，此值乘以源y组件并添加到目标y组件中。
         */
        const val M11 = 5

        /**
         * Yz：通常在X轴上旋转时角度的负正弦。在vector3乘法中，此值乘以源z组件并添加到目标y组件中。
         */
        const val M12 = 9

        /**
         * YW：通常是Y组件的翻译。在Vector3乘法上，此值将添加到目标y组件中。
         */
        const val M13 = 13

        /**
         * ZX：通常在Y轴上旋转时角度的负正弦。在vector3乘法中，此值乘以源x组件并添加到目标z组件中。
         */
        const val M20 = 2

        /**
         * ZY：典型的在x轴上旋转时角度的正弦。在Vector3乘法上，此值乘以源y组件并添加到目标z组件中。
         */
        const val M21 = 6

        /**
         * ZZ：通常是用于缩放缩放的Z分量，当在X和/或Y轴上旋转时角度的余弦。在vector3乘法中，此值乘以源z组件并添加到目标z组件中。
         */
        const val M22 = 10

        /**
         * ZW：通常是Z组件的翻译。在Vector3乘法上，此值将添加到目标Z组件中。
         */
        const val M23 = 14

        /**
         * WX：通常值零。在Vector3乘法上，此值被忽略。
         */
        const val M30 = 3

        /**
         * WY：通常值零。在Vector3乘法上，此值被忽略。
         */
        const val M31 = 7

        /**
         * WZ：通常值零。在Vector3乘法上，此值被忽略。
         */
        const val M32 = 11

        /**
         * WW：通常是值。在Vector3乘法上，此值被忽略。
         */
        const val M33 = 15
    }

    private val values = FloatArray(16)

    constructor() {
        values[M00] = 1f
        values[M11] = 1f
        values[M22] = 1f
        values[M33] = 1f
    }

    constructor(matrix4: Matrix4) {
        matrix4.values.copyInto(values)
    }

    fun identify() {
        IDENTITY.copyInto(values)
    }
}