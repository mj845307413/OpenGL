package com.example.opengl.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.example.opengl.render.Triangle.COORDS_PER_VERTEX;
import static com.example.opengl.utils.OpenGLUtils.loadShader;

/**
 * @author majun
 * @date 2020-03-19
 */
public class BallRender implements GLSurfaceView.Renderer {
    private static final int NUM_SIDE = 10000;
    private static final float CIRCLE_RADIUS = 0.5f;

    private final String vertexShaderCode =
            "uniform mat4 vMatrix;" +
                    "varying vec4 vColor;" +
                    "attribute vec4 vPosition;" +
                    "void main(){" +
                    "    gl_Position=vMatrix*vPosition;" +
                    "    if(vPosition.z!=0.0){" +
                    "        vColor=vec4(0.0,0.0,0.0,1.0);" +
                    "    }else{" +
                    "        vColor=vec4(0.9,0.9,0.9,1.0);" +
                    "    }" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "gl_FragColor = vColor;" +
                    "}";
    private final int vertexCount;
    /**
     * 正方体各个点
     */
    private float[] positions;

    private int mProgram;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private FloatBuffer vertexBuffer;
    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    public BallRender() {
        positions = createPositions();
        vertexCount = positions.length / 3;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //将背景设置为灰色，
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        //开启深度绘制
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //申请底层空间
        ByteBuffer bb = ByteBuffer.allocateDirect(
                positions.length * 4);
        bb.order(ByteOrder.nativeOrder());
        //将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(positions);
        vertexBuffer.position(0);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        //创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram();
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        //将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
//计算宽高比
        float ratio = (float) width / height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, -5.0f, -5.0f, -5.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);

        //指定vMatrix的值,glGetUniformLocation这个是获取uniform的
        int mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);


        //获取顶点着色器的vPosition成员句柄
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);

        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    private float[] createPositions() {
        ArrayList<Float> data = new ArrayList<>();
        //设置圆心坐标
        data.add(0.0f);
        data.add(0.0f);
        //设置高度，形成锥面
        data.add(1f);
        float angDegSpan = 360f / NUM_SIDE;
        for (float i = 0; i < 360 + angDegSpan; i += angDegSpan) {
            data.add((float) (CIRCLE_RADIUS * Math.sin(i * Math.PI / 180f)));
            data.add((float) (CIRCLE_RADIUS * Math.cos(i * Math.PI / 180f)));
            data.add(0.0f);
        }
        float[] f = new float[data.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = data.get(i);
        }
        return f;
    }
}
