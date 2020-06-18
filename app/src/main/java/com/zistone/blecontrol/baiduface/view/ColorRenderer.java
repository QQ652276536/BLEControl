package com.zistone.blecontrol.baiduface.view;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ColorRenderer implements GLSurfaceView.Renderer {
    /**
     * GL图形绘制渲染器
     */
    private ColorViewImpl mColorViewImpl;
    /**
     * 接收的帧数据
     */
    private ByteBuffer mRgbBuffer;

    /**
     * 接收将要绘制的帧数据
     *
     * @param buffer
     */
    public void setRgbBuffer(ByteBuffer buffer) {
        mRgbBuffer = buffer;
    }

    /**
     * 仅调用一次，设置view的OpenGLES环境
     *
     * @param gl
     * @param config
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 设置背景色（用来清除缓冲的颜色为黑色）
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        // 创建GL图形绘制渲染器
        mColorViewImpl = new ColorViewImpl();
        if (!mColorViewImpl.isProgramBuilt()) {
            mColorViewImpl.buildProgram();
        }
        // 打开深度检测
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // 打开背面剪裁
        GLES20.glEnable(GLES20.GL_CULL_FACE);
    }

    /**
     * 如果view的几和形状发生变化了就调用
     *
     * @param gl
     * @param width
     * @param height
     */
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 刷新View的形状
        GLES20.glViewport(0, 0, width, height);
    }

    /**
     * 每次View被重绘时被调用
     *
     * @param gl
     */
    public void onDrawFrame(GL10 gl) {
        if (mColorViewImpl == null) {
            return;
        }
        // 清除缓冲
        // GLES20.GL_COLOR_BUFFER_BIT是颜色缓冲
        // GLES20.GL_DEPTH_BUFFER_BIT是深度缓冲
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        if (mRgbBuffer != null) {
            mRgbBuffer.position(0);
            mColorViewImpl.buildTextures(mRgbBuffer, 640, 480);
        }
        mColorViewImpl.drawSelf();
    }
}
