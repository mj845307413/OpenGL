package com.example.opengl.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author majun
 * @date 2020-03-03
 */
public class ShapeRenderer implements GLSurfaceView.Renderer {
    private Triangle mTriangle;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
// initialize a triangle
        mTriangle = new Triangle();
    }

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] scratch = new float[16];

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        //设置设置视图窗口
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    private float[] rotationMatrix = new float[16];

    @Override
    public void onDrawFrame(GL10 unused) {
        addRotate();
    }


    private void addRotate() {
        // Set the camera position (View matrix)
        //设置相机变换矩阵
        Matrix.setLookAtM(viewMatrix,//相机变换矩阵
                0,//变换矩阵的起始位置（偏移量）
                0, 0, -3,//相机位置
                0f, 0f, 0f,//观测点位置
                0f, 1.0f, 0.0f);//相机的方向

        // Calculate the projection and view transformation
        //矩阵相乘 vPMatrix是相乘的结果
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // Create a rotation for the triangle
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);
        //设置旋转变化矩阵
        Matrix.setRotateM(rotationMatrix, 0, mAngle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);

        // Draw triangle
        mTriangle.draw(scratch);
    }

    private void addAnimation() {

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        Matrix.setRotateM(rotationMatrix, 0, angle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);

        // Draw triangle
        mTriangle.draw(scratch);
    }

    public volatile float mAngle;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }
}
