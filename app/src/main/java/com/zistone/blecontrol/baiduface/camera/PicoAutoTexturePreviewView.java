package com.zistone.blecontrol.baiduface.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;

public class PicoAutoTexturePreviewView extends FrameLayout {

    public TextureView textureView;

    private int videoWidth = 0;
    private int videoHeight = 0;


    private int previewWidth = 0;
    private int previewHeight = 0;
    private static int scale = 2;

    private Paint mPaint;
    private Rect mSrcRect;
    private Rect mDstRect;


    public PicoAutoTexturePreviewView(Context context) {
        super(context);
        init();
    }

    public PicoAutoTexturePreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PicoAutoTexturePreviewView(Context context, AttributeSet attrs,
                                      int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private Handler handler = new Handler(Looper.getMainLooper());

    private void init() {
        textureView = new TextureView(getContext());
        addView(textureView);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mSrcRect = new Rect();
        mDstRect = new Rect();
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
            textureView.layout(0, (previewHeight - scaledChildHeight) / scale,
                    previewWidth, (previewHeight + scaledChildHeight) / scale);
        } else {
            int scaledChildWidth = videoWidth * previewHeight / videoHeight;
            textureView.layout((previewWidth - scaledChildWidth) / scale, 0,
                    (previewWidth + scaledChildWidth) / scale, previewHeight);

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

    public void draw(Bitmap bm) {
        Canvas canvas = textureView.lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mSrcRect.set(0, 0, bm.getWidth(), bm.getHeight());
            mDstRect.set(0, 0, textureView.getWidth(), bm.getHeight() * textureView.getWidth() / bm.getWidth());
            canvas.drawBitmap(bm, mSrcRect, mDstRect, mPaint);
        }
        textureView.unlockCanvasAndPost(canvas);
    }


}
