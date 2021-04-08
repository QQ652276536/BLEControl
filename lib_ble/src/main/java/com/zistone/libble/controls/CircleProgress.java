package com.zistone.libble.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CircleProgress extends View {

    private static final String TAG = "CircleProgress";

    private Paint paint;
    private int now = 0;
    private int max = 0;
    private Rect rect;
    private int rundwidth = 60;//圆弧宽度
    private int measuredWidth;


    public CircleProgress(Context context) {
        this(context, null);
    }

    public CircleProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化
        initView();
    }

    public int getNow() {
        return now;
    }

    public void setNow(int now) {
        Log.i(TAG, "当前值：" + now + "，最大值：" + max);
        this.now = now;
        invalidate();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();//强制重绘
    }

    private void initView() {
        paint = new Paint();
        rect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measuredWidth = getMeasuredWidth();//测量当前画布大小
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (now <= 0 || max <= 0)
            return;
        //设置为空心圆
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);
        paint.setColor(Color.GRAY);
        float x = measuredWidth / 2;
        float y = measuredWidth / 2;
        int rd = measuredWidth / 2 - rundwidth / 2;
        canvas.drawCircle(x, y, rd, paint);
        //绘制圆弧
        RectF rectF = new RectF(rundwidth / 2, rundwidth / 2, measuredWidth - rundwidth / 2, measuredWidth - rundwidth / 2);
        paint.setColor(Color.parseColor("#03DAC5"));
        canvas.drawArc(rectF, 90, now * 360 / max, false, paint);
        //设置当前文字
        String text = now * 100 / max + "%";
        paint.setStrokeWidth(0);

        Rect rect = new Rect();
        paint.setTextSize(50);
        paint.getTextBounds(text, 0, text.length(), rect);
        paint.setColor(Color.WHITE);
        canvas.drawText(text, measuredWidth / 2 - rect.width() / 2, measuredWidth / 2 + rect.height() / 2, paint);
    }
}