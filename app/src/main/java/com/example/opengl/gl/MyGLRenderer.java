package com.example.opengl.gl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author majun
 * @date 2020-03-03
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    /**
     * 调用一次以设置视图的 OpenGL ES 环境。
     *
     * @param gl10
     * @param eglConfig
     */
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //设置背景的颜色
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);

    }

    /**
     * 当视图的几何图形发生变化（例如当设备的屏幕方向发生变化）时调用。
     */
    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width / 2, height / 2);
    }

    /**
     * 每次重新绘制视图时调用
     *
     * @param gl10
     */
    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }
}
