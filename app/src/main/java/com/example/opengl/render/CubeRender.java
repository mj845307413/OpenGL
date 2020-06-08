package com.example.opengl.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.opengl.utils.MatrixTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.example.opengl.render.Triangle.COORDS_PER_VERTEX;
import static com.example.opengl.utils.OpenGLUtils.loadShader;

/**
 * @author majun
 * @date 2020-03-19
 */
public class CubeRender implements GLSurfaceView.Renderer {

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 vMatrix;" +
                    "varying  vec4 vColor;" +
                    "attribute vec4 aColor;" +
                    "void main() {" +
                    "gl_Position = vMatrix*vPosition;" +
                    "vColor=aColor;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "gl_FragColor = vColor;" +
                    "}";
    /**
     * 正方体各个点
     */
    private final float[] cubePositions = {
            -1.0f, 1.0f, 1.0f,    //正面左上0
            -1.0f, -1.0f, 1.0f,   //正面左下1
            1.0f, -1.0f, 1.0f,    //正面右下2
            1.0f, 1.0f, 1.0f,     //正面右上3
            -1.0f, 1.0f, -1.0f,    //反面左上4
            -1.0f, -1.0f, -1.0f,   //反面左下5
            1.0f, -1.0f, -1.0f,    //反面右下6
            1.0f, 1.0f, -1.0f,     //反面右上7
    };

    /**
     * 各个面由哪几个点组成
     */
    final short[] index = {
            0, 3, 2, 0, 2, 1,    //正面
            0, 1, 5, 0, 5, 4,    //左面
            0, 7, 3, 0, 4, 7,    //上面
            6, 7, 4, 6, 4, 5,    //后面
            6, 3, 7, 6, 2, 3,    //右面
            6, 5, 1, 6, 1, 2     //下面
    };

    /**
     * 正方体的各个点的配色
     */
    private float[] color = {
            1f, 0f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 0f, 1f, 1f,
            0f, 1f, 0f, 1f,
            1f, 0f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 0f, 1f, 1f,
            0f, 1f, 0f, 1f,
    };


    private int mProgram;
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;
    private float[] matrix = new float[16];
    private int hMatrix;
    private int hVertex;
    private int hColor;
    private MatrixTools tools;

    public CubeRender() {
        tools = new MatrixTools();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //将背景设置为灰色，
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        //开启深度绘制
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //申请底层空间
        ByteBuffer bb = ByteBuffer.allocateDirect(
                cubePositions.length * 4);
        bb.order(ByteOrder.nativeOrder());
        //将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubePositions);
        vertexBuffer.position(0);


        ByteBuffer dd = ByteBuffer.allocateDirect(
                color.length * 4);
        dd.order(ByteOrder.nativeOrder());
        colorBuffer = dd.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

        ByteBuffer cc = ByteBuffer.allocateDirect(index.length * 2);
        cc.order(ByteOrder.nativeOrder());
        indexBuffer = cc.asShortBuffer();
        indexBuffer.put(index);
        indexBuffer.position(0);


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

        hVertex = GLES20.glGetAttribLocation(mProgram, "vPosition");
        hColor = GLES20.glGetAttribLocation(mProgram, "aColor");
        hMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
        float rate=width/(float)height;
        tools.ortho(-rate*6,rate*6,-6,6,3,20);
        tools.setCamera(10,10,10,0,0,0,0,1,0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        matrix = tools.getFinalMatrix();
        drawSelf();

//y轴正方形平移
        tools.pushMatrix();
        tools.translate(0, 3, 0);
        matrix = tools.getFinalMatrix();
        drawSelf();
        tools.popMatrix();

//y轴负方向平移，然后按xyz->(0,0,0)到(1,1,1)旋转30度
        tools.pushMatrix();
        tools.translate(0, -3, 0);
        tools.rotate(30f, 1, 1, 1);
        matrix = tools.getFinalMatrix();
        drawSelf();
        tools.popMatrix();

//x轴负方向平移，然后按xyz->(0,0,0)到(1,-1,1)旋转120度，在放大到0.5倍
        tools.pushMatrix();
        tools.translate(-3, 0, 0);
        tools.scale(0.5f, 0.5f, 0.5f);

//在以上变换的基础上再进行变换
        tools.pushMatrix();
        tools.translate(12, 0, 0);
        tools.scale(1.0f, 2.0f, 1.0f);
        tools.rotate(30f, 1, 2, 1);
        matrix = tools.getFinalMatrix();
        drawSelf();
        tools.popMatrix();

//接着被中断的地方执行
        tools.rotate(30f, -1, -1, 1);
        matrix = tools.getFinalMatrix();
        drawSelf();
        tools.popMatrix();
    }

    public void drawSelf() {

        //指定vMatrix的值
        if (matrix != null) {
            GLES20.glUniformMatrix4fv(hMatrix, 1, false, matrix, 0);
        }
        //启用句柄
        GLES20.glEnableVertexAttribArray(hVertex);
        GLES20.glEnableVertexAttribArray(hColor);
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(hVertex, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);
        //设置绘制三角形的颜色
        GLES20.glVertexAttribPointer(hColor, 4,
                GLES20.GL_FLOAT, false,
                0, colorBuffer);
        //索引法绘制正方体
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(hVertex);
        GLES20.glDisableVertexAttribArray(hColor);
    }

}
