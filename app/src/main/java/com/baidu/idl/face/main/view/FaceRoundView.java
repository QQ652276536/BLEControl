package com.baidu.idl.face.main.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.baidu.idl.face.main.utils.DensityUtils;

/**
 * 人脸检测区域View
 */
public class FaceRoundView extends View {

    public static final float SURFACE_HEIGHT = 1000f;
    public static final float WIDTH_SPACE_RATIO = 0.3f; // 圆相当于占短边的3/5
    public static final float HEIGHT_RATIO = 0.1f;
    public static final float HEIGHT_EXT_RATIO = 1.3f;
    public static final int CIRCLE_SPACE = 5;
    public static final int PATH_SPACE = 16;
    public static final int PATH_SMALL_SPACE = 12;
    public static final int PATH_WIDTH = 4;

    public static final int COLOR_BG = Color.parseColor("#FFFFFF");
    public static final int COLOR_RECT = Color.parseColor("#FFFFFF");

    public static final int COLOR_ROUND = Color.parseColor("#33CC83");

    private Paint mBGPaint;
    private Paint mFaceRoundPaint;


    private float mX;
    private float mY;
    private float mR;


    public FaceRoundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Context mContext = context;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float pathSpace = DensityUtils.dip2px(context, PATH_SPACE);
        float pathSmallSpace = DensityUtils.dip2px(context, PATH_SMALL_SPACE);
        float pathWidth = DensityUtils.dip2px(context, PATH_WIDTH);
        PathEffect mFaceRoundPathEffect = new DashPathEffect(
                new float[]{pathSpace, dm.heightPixels < SURFACE_HEIGHT
                        ? pathSmallSpace : pathSpace}, 1);

        mBGPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBGPaint.setColor(COLOR_BG);
        mBGPaint.setStyle(Paint.Style.FILL);
        mBGPaint.setAntiAlias(true);
        mBGPaint.setDither(true);

        Paint mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPathPaint.setColor(COLOR_ROUND);
        mPathPaint.setStrokeWidth(pathWidth);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);

        Paint mFaceRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFaceRectPaint.setColor(COLOR_RECT);
        mFaceRectPaint.setStrokeWidth(pathWidth);
        mFaceRectPaint.setStyle(Paint.Style.STROKE);
        mFaceRectPaint.setAntiAlias(true);
        mFaceRectPaint.setDither(true);

        mFaceRoundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFaceRoundPaint.setColor(COLOR_ROUND);
        mFaceRoundPaint.setStyle(Paint.Style.FILL);
        mFaceRoundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mFaceRoundPaint.setAntiAlias(true);
        mFaceRoundPaint.setDither(true);
    }

    public void processDrawState(boolean isDrawDash) {
        boolean mIsDrawDash = true;
        mIsDrawDash = isDrawDash;
        postInvalidate();
    }

    public float getRound() {
        return mR;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        float canvasWidth = right - left;
        float canvasHeight = bottom - top;
        if (canvasWidth >= canvasHeight) {
            // 横屏显示器
            float x = canvasWidth / 2;
            float y = (canvasHeight / 2) - ((canvasHeight / 2) * HEIGHT_RATIO);
            float r = canvasHeight * WIDTH_SPACE_RATIO;
            mX = x;
            mY = y;
            mR = r;
        } else {
            // 竖屏显示器
            float x = canvasWidth / 2;
            float y = canvasHeight / 2;
            float r = canvasWidth * WIDTH_SPACE_RATIO;

            mX = x;
            mY = y;
            mR = r;
        }

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 遮罩
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawPaint(mBGPaint);
        canvas.drawCircle(mX, mY, mR - 2, mFaceRoundPaint);

    }

    public static Rect getPreviewDetectRect(int w, int pw, int ph) {
        float round = (w / 2) - ((w / 2) * WIDTH_SPACE_RATIO);
        float x = pw / 2;
        float y = (ph / 2) - ((ph / 2) * HEIGHT_RATIO);
        float r = (pw / 2) > round ? round : (pw / 2);
        float hr = r + (r * HEIGHT_EXT_RATIO);
        Rect rect = new Rect((int) (x - r),
                (int) (y - hr),
                (int) (x + r),
                (int) (y + hr));
        return rect;
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}