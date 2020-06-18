/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.zistone.blecontrol.baiduface.view;

import android.opengl.GLES20;
import android.util.Log;

public class ImiShaderUtil {

    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        // 创建一个空的OpenGL ES Program
        int program = GLES20.glCreateProgram();
        if (vertexShader == 0 || fragmentShader == 0 || program == 0) {
            return 0;
        }
        // 将vertex shader添加到program
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        // 将fragment shader添加到program
        GLES20.glAttachShader(program, fragmentShader);
        checkGlError("glAttachShader");
        // 创建可执行的 OpenGL ES program
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e("ES20_ERROR", "Could not link program: ");
            Log.e("ES20_ERROR", GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    private static int loadShader(int shaderType, String source) {
        // 创建一个vertex shader类型(GLES20.GL_VERTEX_SHADER)
        // 或fragment shader类型(GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            // 将源码添加到shader
            GLES20.glShaderSource(shader, source);
            // 编译代码
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e("ES20_ERROR", "Could not compile shader " + shaderType
                        + ":");
                Log.e("ES20_ERROR", GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("ES20_ERROR", op + ": glError " + error);
//            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
