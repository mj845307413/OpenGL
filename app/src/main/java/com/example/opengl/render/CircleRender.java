package com.example.opengl.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

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
public class CircleRender implements GLSurfaceView.Renderer {
    private static final int NUM_SIDE = 4;
    private static final float CIRCLE_RADIUS = 0.5f;

    /**
     * 顶点着色器
     * gl_Position是Shader的内置变量，为定点位置
     */
    private static final String vertexShaderCode = "attribute vec4 vPosition;" +
            " void main() {" +
            "     gl_Position = vPosition;" +
            " }";

    /**
     * 片段着色器
     * gl_FragColor都是Shader的内置变量，为片元颜色。
     */
    private static final String fragmentShaderCode =
            " precision mediump float;" +
                    " uniform vec4 vColor;" +
                    " void main() {" +
                    "     gl_FragColor = vColor;" +
                    " }";


    float color[] = {1.0f, 1.0f, 1.0f, 1.0f}; //白色
    private FloatBuffer vertexBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个点的字节大小

    private int vertexCount;
    private float[] shapeData;

    public CircleRender() {
        shapeData = createPositions();
        vertexCount = shapeData.length / COORDS_PER_VERTEX;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //将背景设置为灰色，
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        //申请底层空间
        ByteBuffer bb = ByteBuffer.allocateDirect(
                shapeData.length * 4);
        bb.order(ByteOrder.nativeOrder());
        //将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(shapeData);
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
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        //获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        //获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        //绘制多边形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    private float[] createPositions() {
        ArrayList<Float> data = new ArrayList<>();
        data.add(0.0f);             //设置圆心坐标
        data.add(0.0f);
        data.add(0.0f);
        float angDegSpan = 360f / CircleRender.NUM_SIDE;
        for (float i = 0; i < 360 + angDegSpan; i += angDegSpan) {
            data.add((float) (CircleRender.CIRCLE_RADIUS * Math.sin(i * Math.PI / 180f)));
            data.add((float) (CircleRender.CIRCLE_RADIUS * Math.cos(i * Math.PI / 180f)));
            data.add(0.0f);
        }
        float[] f = new float[data.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = data.get(i);
        }
        return f;
    }
}
