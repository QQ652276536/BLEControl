package com.zistone.libble.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class MyBarChartView extends View {

    private int barInterval;
    private int barWidth;
    private int top_text_size;
    private int bottom_text_size;
    private int bar_color;
    private int bottom_line_color;
    private int top_text_color;
    private int bottom_text_color;
    private Paint mTopTextPaint;
    private Paint mBottomTextPaint;
    private Paint mBarPaint;
    private Paint mBottomLinePaint;
    private ArrayList<BarData> innerData = new ArrayList<>();
    private int paddingTop;
    private int paddingLeft;
    private int paddingBottom;
    private int paddingRight;
    private int defaultHeight = dp2Px(180);
    private int bottom_view_height = dp2Px(30);
    private int top_text_height = dp2Px(30);
    private float scaleTimes = 1;
    private float lastX = 0;
    private float lastY = 0;
    private int measureWidth = 0;
    //这是最初的的位置
    private float startOriganalX = 0;
    private HorizontalScrollRunnable horizontalScrollRunnable;
    //临时滑动的距离
    private float tempLength = 0;
    private long startTime = 0;
    private boolean isFling = false;
    private float dispatchTouchX = 0;
    private float dispatchTouchY = 0;
    //是否到达边界
    private boolean isBoundary = false;
    private boolean isMove = false;

    public MyBarChartView(Context context) {
        this(context, null);
    }

    public MyBarChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyBarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        barInterval = 60;
        bar_color = Color.GREEN;
        barWidth = 30;
        top_text_size = 20;
        top_text_color = Color.BLACK;
        bottom_text_size = 20;
        bottom_text_color = Color.BLACK;
        bottom_line_color = Color.YELLOW;
        initPaint();


    }

    private int dp2Px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private int sp2Px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private void initPaint() {
        mTopTextPaint = new Paint();
        mTopTextPaint.setTextSize(top_text_size);
        mTopTextPaint.setColor(top_text_color);
        mTopTextPaint.setStrokeCap(Paint.Cap.ROUND);
        mTopTextPaint.setStyle(Paint.Style.FILL);
        mTopTextPaint.setDither(true);

        mBottomTextPaint = new Paint();
        mBottomTextPaint.setTextSize(bottom_text_size);
        mBottomTextPaint.setColor(bottom_text_color);
        mBottomTextPaint.setStrokeCap(Paint.Cap.ROUND);
        mBottomTextPaint.setStyle(Paint.Style.FILL);
        mBottomTextPaint.setDither(true);


        mBarPaint = new Paint();
        mBarPaint.setTextSize(top_text_size);
        mBarPaint.setColor(bar_color);
        mBarPaint.setStrokeCap(Paint.Cap.ROUND);
        mBarPaint.setStyle(Paint.Style.FILL);
        mBarPaint.setDither(true);


        mBottomLinePaint = new Paint();
        mBottomLinePaint.setTextSize(top_text_size);
        mBottomLinePaint.setColor(bottom_line_color);
        mBottomLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mBottomLinePaint.setStyle(Paint.Style.FILL);
        mBottomLinePaint.setDither(true);
        //设置底部线的宽度
        mBottomLinePaint.setStrokeWidth(dp2Px(1f));


    }

    public void setBarChartData(List<BarData> innerData) {
        this.innerData.clear();
        this.innerData.addAll(innerData);
        scaleTimes = (float) getMaxValue() / (float) (defaultHeight - bottom_view_height - top_text_height);
        invalidate();
    }

    private int getMaxValue() {
        int defaultValue = 0;
        if (innerData.size() > 0) {
            defaultValue = innerData.get(0).getCount();
            for (int i = 0; i < innerData.size(); i++) {
                if (innerData.get(i).getCount() > defaultValue) {
                    defaultValue = innerData.get(i).getCount();
                }
            }
        }
        return defaultValue;
    }

    //进行滑动的边界处理
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e("TAG", "MyBarChartView===dispatchTouchEvent==" + ev.getAction());
        int dispatchCurrX = (int) ev.getX();
        int dispatchCurrY = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //父容器不拦截点击事件，子控件拦截点击事件。如果不设置为true,外层会直接拦截，从而导致motionEvent为cancle
                getParent().requestDisallowInterceptTouchEvent(true);
                dispatchTouchX = getX();
                dispatchTouchY = getY();
                break;
            case MotionEvent.ACTION_MOVE:

                float deltaX = dispatchCurrX - dispatchTouchX;
                float deltaY = dispatchCurrY - dispatchTouchY;
                if (Math.abs(deltaY) - Math.abs(deltaX) > 0) {//竖直滑动的父容器拦截事件
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                //这是向右滑动，如果是滑动到边界，那么就让父容器进行拦截
                if ((dispatchCurrX - dispatchTouchX) > 0 && startOriganalX == 0) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else if ((dispatchCurrX - dispatchTouchX) < 0 && startOriganalX == -getMoveLength()) {//这是向右滑动
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        dispatchTouchX = dispatchCurrX;
        dispatchTouchY = dispatchCurrY;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        isBoundary = false;
        isMove = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                startTime = System.currentTimeMillis();
                //当点击的时候，判断如果是在fling的效果的时候，就停止快速滑动
                if (isFling) {
                    removeCallbacks(horizontalScrollRunnable);
                    isFling = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float currX = event.getX();
                float currY = event.getY();
                startOriganalX += currX - lastX;

                //这是向右滑动
                if ((currX - lastX) > 0) {
                    Log.e("TAG", "向右滑动");
                    if (startOriganalX > 0) {
                        startOriganalX = 0;
                        isBoundary = true;
                    }

                } else {//这是向右滑动
                    Log.e("TAG", "向左滑动");
                    if (-startOriganalX > getMoveLength()) {
                        startOriganalX = -getMoveLength();
                        isBoundary = true;
                    }
                }
                tempLength = currX - lastX;
                //如果数据量少，根本没有充满横屏，就没必要重新绘制，
                if (measureWidth < innerData.size() * (barWidth + barInterval)) {
                    invalidate();
                }

                lastX = currX;
                lastY = currY;
                break;
            case MotionEvent.ACTION_UP:
                long endTime = System.currentTimeMillis();
                //计算猛滑动的速度，如果是大于某个值，并且数据的长度大于整个屏幕的长度，那么就允许有flIng后逐渐停止的效果
                float speed = tempLength / (endTime - startTime) * 1000;
                if (Math.abs(speed) > 100 && !isFling && measureWidth < innerData.size() * (barWidth + barInterval)) {
                    this.post(horizontalScrollRunnable = new HorizontalScrollRunnable(speed));
                }
                isMove = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                isMove = false;
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        drawBottomLine(canvas);

        //如果没有数据 绘制loading...
        if (innerData.size() <= 0) {
            drawNoDataText(canvas);

        } else {
            int startX = (int) (paddingLeft + startOriganalX);
            int endY = defaultHeight - bottom_view_height;

            for (int i = 0; i < innerData.size(); i++) {
                String txt = innerData.get(i).getBottomText();
                int value = innerData.get(i).getCount();
                float barHeight = 0;
                if (scaleTimes != 0) {
                    barHeight = (float) value / scaleTimes;
                }
                int startY = (int) (defaultHeight - bottom_view_height - barHeight);

                float topTextWidth = mTopTextPaint.measureText(value + "");
                float textStartX = startX + barWidth / 2 - topTextWidth / 2;
                float textStartY = startY - 10;
                //绘制bar上的文字
                drawTopText(canvas, value + "", textStartX, textStartY);
                //绘制bar
                drawBar(canvas, startX, startY, endY, txt);
                //绘制下面的文字
                float bottomTextWidth = mBottomTextPaint.measureText(innerData.get(i).bottomText);
                float bottomStartX = startX + barWidth / 2 - bottomTextWidth / 2;
                Rect rect = new Rect();
                mBottomTextPaint.getTextBounds(txt, 0, txt.length(), rect);
                float bottomStartY = defaultHeight - bottom_view_height + 10 + rect.height();//rect.height()是获取文本的高度;
                //绘制底部的文字
                drawBottomText(canvas, txt, bottomStartX, bottomStartY);

                startX = startX + barWidth + barInterval;
            }
        }

    }

    private void drawBottomLine(Canvas canvas) {
        canvas.drawLine(paddingLeft, defaultHeight - bottom_view_height, innerData.size() * (barWidth + barInterval),
                defaultHeight - bottom_view_height, mBottomLinePaint);
    }

    private void drawNoDataText(Canvas canvas) {
        String text = "loading...";
        float textWidth = mBottomTextPaint.measureText(text);
        canvas.drawText(text, measureWidth / 2 - textWidth / 2, defaultHeight / 2 - 10, mBottomTextPaint);
    }

    //绘制bar上的文字
    private void drawTopText(Canvas canvas, String text, float textStartX, float textStartY) {
        canvas.drawText(text, textStartX, textStartY, mTopTextPaint);
    }

    //绘制bar
    private void drawBar(Canvas canvas, int startX, int startY, int endY, String type) {
        if (type.contains("GPS")) {
            mBarPaint.setColor(Color.BLUE);
        } else if (type.contains("北斗")) {
            mBarPaint.setColor(Color.GREEN);
        } else {
            mBarPaint.setColor(Color.RED);
        }
        Rect mRect = new Rect(startX, startY, startX + barWidth, endY);
        canvas.drawRect(mRect, mBarPaint);

    }

    private void drawBottomText(Canvas canvas, String text, float bottomStartX, float bottomStartY) {
        canvas.drawText(text, bottomStartX, bottomStartY, mBottomTextPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = 0;
        int height = 0;

        if (widthMode == MeasureSpec.EXACTLY) {
            measureWidth = width = widthSize;
        } else {
            width = getAndroiodScreenProperty().get(0);
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            defaultHeight = height = heightSize;
        } else {
            height = defaultHeight;
        }
        setMeasuredDimension(width, height);
        paddingTop = getPaddingTop();
        paddingLeft = getPaddingLeft();
        paddingBottom = getPaddingBottom();
        paddingRight = getPaddingRight();


    }

    private ArrayList<Integer> getAndroiodScreenProperty() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）
        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        int screenHeight = (int) (height / density);// 屏幕高度(dp)

        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(screenWidth);
        integers.add(screenHeight);
        return integers;
    }

    private int getMoveLength() {
        return (barWidth + barInterval) * innerData.size() - measureWidth;
    }

    public boolean isBoundary() {
        return isBoundary;
    }

    public boolean isMove() {
        return isMove;
    }

    public static class BarData implements Comparable<BarData> {
        private int count;
        private String bottomText;

        public BarData(int count, String bottomText) {
            this.count = count;
            this.bottomText = bottomText;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getBottomText() {
            return bottomText == null ? "" : bottomText;
        }

        public void setBottomText(String bottomText) {
            this.bottomText = bottomText;
        }

        @Override
        public int compareTo(BarData o) {
            int v1 = Integer.parseInt(this.getBottomText().replace("GPS", "").replace("北斗", ""));
            int v2 = Integer.parseInt(o.getBottomText().replace("GPS", "").replace("北斗", ""));
            return v1 - v2;
        }
    }

    private class HorizontalScrollRunnable implements Runnable {

        private float speed = 0;

        public HorizontalScrollRunnable(float speed) {
            this.speed = speed;
        }

        @Override
        public void run() {
            if (Math.abs(speed) < 30) {
                isFling = false;
                return;
            }
            isFling = true;
            startOriganalX += speed / 15;
            speed = speed / 1.15f;
            //这是向右滑动
            if ((speed) > 0) {
                Log.e("TAG", "向右滑动");
                if (startOriganalX > 0) {
                    startOriganalX = 0;
                }

            } else {//这是向右滑动
                Log.e("TAG", "向左滑动");
                if (-startOriganalX > getMoveLength()) {
                    startOriganalX = -getMoveLength();
                }
            }
            postDelayed(this, 20);
            invalidate();
        }
    }
}


