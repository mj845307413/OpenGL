package com.example.opengl.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.example.opengl.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.example.opengl.utils.OpenGLUtils.loadShader;

/**
 * @author majun
 * @date 2020-03-22
 */
public class BitmapRender implements GLSurfaceView.Renderer {
    /**
     * 顶点着色器
     * gl_Position是Shader的内置变量，为定点位置
     */
    private static final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 vCoordinate;" +
                    "uniform mat4 vMatrix;" +
                    "" +
                    "varying vec2 aCoordinate;" +
                    "varying vec4 aPos;" +
                    "varying vec4 gPosition;" +
                    "" +
                    "void main(){" +
                    "    gl_Position=vMatrix*vPosition;" +
                    "    aPos=vPosition;" +
                    "    aCoordinate=vCoordinate;" +
                    "    gPosition=vMatrix*vPosition;" +
                    "}";

    /**
     * 片段着色器
     * gl_FragColor都是Shader的内置变量，为片元颜色。
     */
    private static final String fragmentShaderCode =
            "precision mediump float;" +
                    "" +
                    "uniform sampler2D vTexture;" +
                    "uniform int vChangeType;" +
                    "uniform vec3 vChangeColor;" +
                    "uniform int vIsHalf;" +
                    "uniform float uXY;" +
                    "" +
                    "varying vec4 gPosition;" +
                    "" +
                    "varying vec2 aCoordinate;" +
                    "varying vec4 aPos;" +
                    "" +
                    "void modifyColor(vec4 color){" +
                    "    color.r=max(min(color.r,1.0),0.0);" +
                    "    color.g=max(min(color.g,1.0),0.0);" +
                    "    color.b=max(min(color.b,1.0),0.0);" +
                    "    color.a=max(min(color.a,1.0),0.0);" +
                    "}" +
                    "" +
                    "void main(){" +
                    "    vec4 nColor=texture2D(vTexture,aCoordinate);" +
                    "    if(aPos.x>0.0||vIsHalf==0){" +
                    "        if(vChangeType==1){" +
                    "            float c=nColor.r*vChangeColor.r+nColor.g*vChangeColor.g+nColor.b*vChangeColor.b;" +
                    "            gl_FragColor=vec4(c,c,c,nColor.a);" +
                    "        }else if(vChangeType==2){" +
                    "            vec4 deltaColor=nColor+vec4(vChangeColor,0.0);" +
                    "            modifyColor(deltaColor);" +
                    "            gl_FragColor=deltaColor;" +
                    "        }else if(vChangeType==3){" +
                    "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.r,aCoordinate.y-vChangeColor.r));" +
                    "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.r,aCoordinate.y+vChangeColor.r));" +
                    "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.r,aCoordinate.y-vChangeColor.r));" +
                    "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.r,aCoordinate.y+vChangeColor.r));" +
                    "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.g,aCoordinate.y-vChangeColor.g));" +
                    "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.g,aCoordinate.y+vChangeColor.g));" +
                    "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.g,aCoordinate.y-vChangeColor.g));" +
                    "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.g,aCoordinate.y+vChangeColor.g));" +
                    "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.b,aCoordinate.y-vChangeColor.b));" +
                    "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.b,aCoordinate.y+vChangeColor.b));" +
                    "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.b,aCoordinate.y-vChangeColor.b));" +
                    "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.b,aCoordinate.y+vChangeColor.b));" +
                    "            nColor/=13.0;" +
                    "            gl_FragColor=nColor;" +
                    "        }else if(vChangeType==4){" +
                    "            float dis=distance(vec2(gPosition.x,gPosition.y/uXY),vec2(vChangeColor.r,vChangeColor.g));" +
                    "            if(dis<vChangeColor.b){" +
                    "                nColor=texture2D(vTexture,vec2(aCoordinate.x/2.0+0.25,aCoordinate.y/2.0+0.25));" +
                    "            }" +
                    "            gl_FragColor=nColor;" +
                    "        }else{" +
                    "            gl_FragColor=nColor;" +
                    "        }" +
                    "    }else{" +
                    "        gl_FragColor=nColor;" +
                    "    }" +
                    "}";

    /**
     * 图形展示区域
     */
    private final float[] sPos = {
            -1.0f, 1.0f,    //左上角
            -1.0f, -1.0f,   //左下角
            1.0f, 1.0f,     //右上角
            1.0f, -1.0f     //右下角
    };

    /**
     * 纹理坐标
     */
    private final float[] sCoord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    private float[] colors = {1f, 1f, 1f};

    private Context mContext;


    private FloatBuffer positionBuffer;
    private FloatBuffer mTextureBuffer;


    private int mProgram;
    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private Bitmap mBitmap;
    private int glHCoordinate;
    private int glHTexture;
    private int glHMatrix;
    private int glHPosition;
    private int glHUxy;
    private int hChangeType;
    private int hChangeColor;
    private int hIsHalf;
    private float uXY;

    public BitmapRender(Context context) {
        mContext = context;
        initBitmap();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //将背景设置为灰色，
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        createShowPosition();
        createTexturePosition();
        initProgram();
        initHandler();
    }

    private void createShowPosition() {
        //申请底层空间
        ByteBuffer bb = ByteBuffer.allocateDirect(
                sPos.length * 4);
        bb.order(ByteOrder.nativeOrder());
        //将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        positionBuffer = bb.asFloatBuffer();
        positionBuffer.put(sPos);
        positionBuffer.position(0);
    }

    private void createTexturePosition() {
        //申请底层空间
        ByteBuffer aa = ByteBuffer.allocateDirect(
                sCoord.length * 4);
        aa.order(ByteOrder.nativeOrder());
        //将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        mTextureBuffer = aa.asFloatBuffer();
        mTextureBuffer.put(sCoord);
        mTextureBuffer.position(0);
    }

    private void initProgram() {
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

    private void initHandler() {
        glHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        glHCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
        glHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");

        glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
        hIsHalf = GLES20.glGetUniformLocation(mProgram, "vIsHalf");
        glHUxy = GLES20.glGetUniformLocation(mProgram, "uXY");
        hChangeType = GLES20.glGetUniformLocation(mProgram, "vChangeType");
        hChangeColor = GLES20.glGetUniformLocation(mProgram, "vChangeColor");

    }

    private void initBitmap() {
        mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.picture);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        float sWH = w / (float) h;
        float sWidthHeight = width / (float) height;
        uXY = sWidthHeight;
        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1, 1, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1, 1, 3, 7);
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight, sWH / sWidthHeight, 3, 7);
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//        GLES20.glUseProgram(mProgram);
//        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);
//
//        GLES20.glEnableVertexAttribArray(mPositionHandle);
//
//        GLES20.glEnableVertexAttribArray(glHCoordinate);
//        GLES20.glUniform1i(hChangeType, 1);
//        GLES20.glUniform3fv(hChangeColor, 1, colors, 0);
//
//        GLES20.glUniform1i(glHTexture, 0);
//        GLES20.glUniform1i(hIsHalf, 0);
//
//        createTexture();
//        //传入顶点坐标
//        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, positionBuffer);
//        //传入纹理坐标
//        GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        GLES20.glUniform1i(hChangeType, 4);
        GLES20.glUniform3fv(hChangeColor, 1, colors, 0);

        GLES20.glUniform1i(hIsHalf, 0);
        GLES20.glUniform1f(glHUxy, uXY);
        GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0);
        GLES20.glEnableVertexAttribArray(glHPosition);
        GLES20.glEnableVertexAttribArray(glHCoordinate);
        GLES20.glUniform1i(glHTexture, 0);
        createTexture();
        GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, positionBuffer);
        GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    private int createTexture() {
        int[] texture = new int[1];
        if (mBitmap != null && !mBitmap.isRecycled()) {
            //生成纹理
            GLES20.glGenTextures(1, texture, 0);
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
            return texture[0];
        }
        return 0;
    }
}
