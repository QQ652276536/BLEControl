package com.zistone.blecontrol.baiduface.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

public class PreviewTexture extends ViewGroup implements TextureView.SurfaceTextureListener {
    TextureView mTextureView;
    Camera.Size mPreviewSize;
    List<Camera.Size> mSupportedPreviewSizes;
    Camera mCamera;
    boolean mPreviewed = false;
    private boolean mSurfaceCreated = false;
    private SurfaceTexture mSurfaceTexture;
    private int previewWidth;
    private int previewHeight;
    private boolean mirrored = true;

    public PreviewTexture(Context context, TextureView textureView) {
        super(context);
        mTextureView = textureView;
        mSurfaceTexture = mTextureView.getSurfaceTexture();
        mTextureView.setSurfaceTextureListener(this);
        //  setBackgroundColor(Color.TRANSPARENT);
    }

    public void setCamera(Camera camera, int width, int height) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
            // get Camera parameters
            Camera.Parameters params = mCamera.getParameters();
            params.setPreviewSize(width, height);
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // set the focus mode
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                // set Camera parameters
            }
            mCamera.setParameters(params);
            if (!mPreviewed && mSurfaceCreated) {
                try {
                    mCamera.setPreviewTexture(mSurfaceTexture);
                    mCamera.startPreview();
                    mPreviewed = true;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public void release() {
        mPreviewed = false;
        mCamera = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int i, int i1) {
        try {
            if (mCamera != null && !mPreviewed) {
                mSurfaceTexture = texture;
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.startPreview();
                mPreviewed = true;
                mSurfaceCreated = true;
            }
        } catch (IOException exception) {
            Log.e("chaixiaogang", "IOException caused by setPreviewDisplay()", exception);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int i, int i1) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        if (mCamera != null) {
            // mCamera.stopPreview();
            mPreviewed = false;
//            mCamera.setPreviewCallback(null);
//            mCamera.stopPreview();
//            mCamera.release();
//            mCamera = null;
        }
        mSurfaceCreated = false;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);
            final int width = r - l;
            final int height = b - t;
            previewWidth = width;
            previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }
            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double aspectTolerance = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > aspectTolerance) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void mapFromOriginalRect(RectF rectF) {
        int selfWidth = getWidth();
        int selfHeight = getHeight();
        if (previewWidth == 0 || previewHeight == 0 || selfWidth == 0 || selfHeight == 0) {
            return;
            // TODO
        }
        Matrix matrix = new Matrix();
        PreviewView.ScaleType scaleType = resolveScaleType();
        if (scaleType == PreviewView.ScaleType.FIT_HEIGHT) {
            int targetWith = previewWidth * selfHeight / previewHeight;
            int delta = (targetWith - selfWidth) / 2;
            float ratio = 1.0f * selfHeight / previewHeight;
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(-delta, 0);
        } else {
            int targetHeight = previewHeight * selfWidth / previewWidth;
            int delta = (targetHeight - selfHeight) / 2;
            float ratio = 1.0f * selfWidth / previewWidth;
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(0, -delta);
        }
        matrix.mapRect(rectF);
        if (mirrored) {
            float left = selfWidth - rectF.right;
            float right = left + rectF.width();
            rectF.left = left;
            rectF.right = right;
        }
    }

    private PreviewView.ScaleType resolveScaleType() {
        float selfRatio = 1.0f * getWidth() / getHeight();
        float targetRatio = 1.0f * previewWidth / previewHeight;
        PreviewView.ScaleType scaleType = this.scaleType;
        if (this.scaleType == PreviewView.ScaleType.CROP_INSIDE) {
            scaleType = selfRatio > targetRatio ? PreviewView.ScaleType.FIT_WIDTH : PreviewView.ScaleType.FIT_HEIGHT;
        }
        return scaleType;
    }

    private PreviewView.ScaleType scaleType = PreviewView.ScaleType.CROP_INSIDE;
}
