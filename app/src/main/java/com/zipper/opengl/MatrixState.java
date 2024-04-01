package com.zipper.opengl;

import android.opengl.Matrix;

import java.util.Arrays;
import java.util.Stack;

/**
 * 矩阵操作类Ma
 * @author zhangzhipeng
 * @date 2024/4/1
 */
public class MatrixState {
    /**
     * 投影矩阵
     */
    private float[] mProjMatrix = new float[16];
    /**
     * 摄像机视图矩阵
     */
    private float[] mVMatrix = new float[16];
    /**
     * 模型矩阵
     */
    private float[] mMVPMatrix = new float[16];
    /**
     * 当前变换矩阵
     */
    float[] currentMatrix = {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};

    Stack<float[]> matrixStack = new Stack<>();

    /**
     * 获取最终矩阵
     *
     * @return
     */
    public float[] getFinalMatrix() {
        // 总物体变化矩阵
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, this.mVMatrix, 0, this.currentMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, this.mProjMatrix, 0, mvpMatrix, 0);
        return mvpMatrix;
    }

    /**
     * 设置正交投影
     *
     * @param left   左平面
     * @param right  右平面
     * @param bottom 下平面
     * @param top    上平面
     * @param near   近平面 相对视点
     * @param far    远平面 相对视点
     */
    public void orthoM(float left, float right, float bottom, float top, float near, float far) {
        Matrix.orthoM(this.mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    public void c() {
        this.currentMatrix = this.matrixStack.pop();
    }

    public void pushStack() {
        // 当前变换矩阵中的各元素入栈
        this.matrixStack.push(Arrays.copyOf(this.currentMatrix, 16));
    }

    public void scaleM(float x, float y, float z) {
        Matrix.scaleM(this.currentMatrix, 0, x, y, z);
    }

    /**
     * 设置相机位置
     */
    public void setLookAtM(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        Matrix.setLookAtM(this.mVMatrix, 0,
                eyeX, eyeY, eyeZ,  // 摄像机位置xyz坐标
                centerX, centerY, centerZ, // 观察目标点xyz坐标
                upX, upY, upZ  // 摄像机UP向量xyz坐标
        );
    }

    /**
     * 将当前变换矩阵沿着xyz轴平移
     */
    public void translateM(float x, float y, float z) {
        Matrix.translateM(this.currentMatrix, 0, x, y, z);
    }
}
