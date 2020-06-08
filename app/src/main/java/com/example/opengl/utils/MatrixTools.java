package com.example.opengl.utils;

import android.opengl.Matrix;

import java.util.Arrays;
import java.util.Stack;

/**
 * @author majun
 * @date 2020/3/28
 */
public class MatrixTools {

    private float[] mMatrixCamera = new float[16];    //相机矩阵
    private float[] mMatrixProjection = new float[16];    //投影矩阵
    private float[] mMatrixCurrent =     //原始矩阵
            {1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1};

    private Stack<float[]> mStack;      //变换矩阵堆栈

    public MatrixTools() {
        mStack = new Stack<>();
    }

    //保护现场
    public void pushMatrix() {
        mStack.push(Arrays.copyOf(mMatrixCurrent, 16));
    }

    //恢复现场
    public void popMatrix() {
        mMatrixCurrent = mStack.pop();
    }

    public void clearStack() {
        mStack.clear();
    }

    //平移变换
    public void translate(float x, float y, float z) {
        Matrix.translateM(mMatrixCurrent, 0, x, y, z);
    }

    //旋转变换
    public void rotate(float angle, float x, float y, float z) {
        Matrix.rotateM(mMatrixCurrent, 0, angle, x, y, z);
    }

    //缩放变换
    public void scale(float x, float y, float z) {
        Matrix.scaleM(mMatrixCurrent, 0, x, y, z);
    }

    //设置相机的角度
    public void setCamera(float ex, float ey, float ez, float cx, float cy, float cz, float ux, float uy, float uz) {
        Matrix.setLookAtM(mMatrixCamera, 0, ex, ey, ez, cx, cy, cz, ux, uy, uz);
    }

    public void frustum(float left, float right, float bottom, float top, float near, float far) {
        Matrix.frustumM(mMatrixProjection, 0, left, right, bottom, top, near, far);
    }

    /**
     * Computes an orthographic projection matrix.
     * 正交投影矩阵
     * @param mOffset 偏移量，默认为 0 ,不偏移
     * @param left 左平面距离
     * @param right 右平面距离
     * @param bottom 下平面距离
     * @param top 上平面距离
     * @param near 近平面距离
     * @param far 远平面距离
     */
    public void ortho(float left, float right, float bottom, float top, float near, float far) {
        Matrix.orthoM(mMatrixProjection, 0, left, right, bottom, top, near, far);
    }

    public float[] getFinalMatrix() {
        float[] ans = new float[16];
        Matrix.multiplyMM(ans, 0, mMatrixCamera, 0, mMatrixCurrent, 0);
        Matrix.multiplyMM(ans, 0, mMatrixProjection, 0, ans, 0);
        return ans;
    }

}
