package com.example.opengl.utils;

import android.opengl.GLES20;

/**
 * @author majun
 * @date 2020-03-18
 */
public class OpenGLUtils {
    /**
     * 这个类主要用于编译C++的代码
     * 这边主要就是生成顶点着色程序和片段着色程序
     *
     * @param type
     * @param shaderCode
     * @return
     */
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
