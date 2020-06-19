/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.face.main.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;

import com.baidu.idl.face.main.model.SingleBaseConfig;

/**
 * 基于系统TextureView实现的预览View
 */
public class AutoTexturePreviewView extends FrameLayout {

    public TextureView textureView;

    private int videoWidth = 0;
    private int videoHeight = 0;


    private int previewWidth = 0;
    private int previewHeight = 0;
    private static int scale = 2;

    public static float circleRadius;
    public static float circleX;
    public static float circleY;

    private float[] pointXY = new float[3];


    public AutoTexturePreviewView(Context context) {
        super(context);
        init();
    }

    public AutoTexturePreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoTexturePreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private Handler handler = new Handler(Looper.getMainLooper());

    private void init() {
        setWillNotDraw(false);
        textureView = new TextureView(getContext());
        addView(textureView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        previewWidth = getWidth();
        previewHeight = getHeight();

        if (videoWidth == 0 || videoHeight == 0 || previewWidth == 0 || previewHeight == 0) {
            return;
        }

        if (previewWidth * videoHeight > previewHeight * videoWidth) {
            int scaledChildHeight = videoHeight * previewWidth / videoWidth;
            textureView.layout(0, (previewHeight - scaledChildHeight) / scale, previewWidth, (previewHeight + scaledChildHeight) / scale);
        } else {
            int scaledChildWidth = videoWidth * previewHeight / videoHeight;
            textureView.layout((previewWidth - scaledChildWidth) / scale, 0, (previewWidth + scaledChildWidth) / scale, previewHeight);

        }


    }

    public TextureView getTextureView() {
        return textureView;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void setPreviewSize(int width, int height) {
        if (this.videoWidth == width && this.videoHeight == height) {
            return;
        }
        this.videoWidth = width;
        this.videoHeight = height;
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });

    }


    @Override
    protected void onDraw(Canvas canvas) {
        String displayType = SingleBaseConfig.getBaseConfig().getDetectFrame();
        if (displayType.equals("fixedarea")) {
            Path path = new Path();
            // 设置裁剪的圆心坐标，半径
            path.addCircle(getWidth() / 2, getHeight() / 2, getWidth() / 3, Path.Direction.CCW);
            // 裁剪画布，并设置其填充方式
            canvas.clipPath(path, Region.Op.REPLACE);

            circleRadius = getWidth() / 3;
            circleX = (getRight() - getLeft()) / 2;
            circleY = (getBottom() - getTop()) / 2;
        }
        super.onDraw(canvas);
    }
}
