package com.zistone.blecontrol.baiduface.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.nio.ByteBuffer;

public class ColorSurfaceView extends GLSurfaceView {
    /**
     * 深度图的图像渲染器
     */
    private ColorRenderer mRenderer;

    public ColorSurfaceView(Context context) {
        super(context);
        init();
    }

    public ColorSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        // 创建一个OpenGL ES 2.0 context，将要使用2.0版的API
        setEGLContextClientVersion(2);
        // 创建一个渲染器
        mRenderer = new ColorRenderer();
        // 设置渲染器
        setRenderer(mRenderer);
        // 设置渲染模式
        // GLSurfaceView.RENDERMODE_CONTINUOUSLY：持续型模式，以一定周期定时重新绘制view
        // GLSurfaceView.RENDERMODE_WHEN_DIRTY：通知型模式，只有在绘制数据改变时才绘制view
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /**
     * 渲染深度图数据
     *
     * @param buffer
     */
    public void updateVertices(ByteBuffer buffer) {
        // 接收视频流的帧数据
        mRenderer.setRgbBuffer(buffer);
        // 执行画面渲染，调用Renderer的相关方法
        requestRender();
    }
}
