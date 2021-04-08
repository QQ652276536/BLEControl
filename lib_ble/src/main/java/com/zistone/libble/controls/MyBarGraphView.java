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

/**
 * 柱状图控件
 *
 * @author LiWei
 * @date 2021/2/19 16:17
 * @email 652276536@qq.com
 */
public class MyBarGraphView extends View {

    /**
     * 实现柱状图可以左右滑动的线程
     */
    private class HorizontalScrollRunnable implements Runnable {

        private float _speed;

        public HorizontalScrollRunnable(float speed) {
            this._speed = speed;
        }

        @Override
        public void run() {
            if (Math.abs(_speed) < 30) {
                _isScrolling = false;
                return;
            }
            _isScrolling = true;
            _afterScrollDraw += _speed / 15;
            _speed = _speed / 1.15f;
            //向右滑动
            if ((_speed) > 0) {
                if (_afterScrollDraw > 0) {
                    _afterScrollDraw = 0;
                }
            }
            //向左滑动
            else {
                if (-_afterScrollDraw > GetScreenOutsideLength()) {
                    _afterScrollDraw = -GetScreenOutsideLength();
                }
            }
            postDelayed(this, 20);
            invalidate();
        }
    }

    private static final String TAG = "MyBarGraphView";

    //横向滑动的线程
    private HorizontalScrollRunnable _hScrollRunnable;
    //屏幕的宽度
    private int _screenWidth = 0;
    //控件的高度
    private int _height = DpToPx(180);
    //柱状图之间的间隔
    private int _barInterval = 50;
    //柱状图的宽度
    private int _barWidth = 30;
    //柱状图顶部的文字大小
    private int _topTxtSize = 18;
    //柱状图顶部文字的颜色
    private int _topTxtColor = Color.RED;
    //柱状图底部（X轴下面）的文字大小
    private int _bottomTxtSize = 22;
    //柱状图底部（X轴下面）的文字颜色
    private int _bottomTxtColor = Color.RED;
    //柱状图的颜色
    private int _barColor = Color.GREEN;
    //X轴的颜色
    private int _xLineColor = Color.RED;
    //绘制柱状图顶部文字的画笔
    private Paint _topTxtPaint;
    //绘制柱状图底部（X轴下面）文字的画笔
    private Paint _bottomTxtPaint;
    //绘制柱状图的画笔
    private Paint _barPaint;
    //绘制X轴的画笔
    private Paint _xLinePaint;
    //绘制柱状图的区域，绘制柱状图底部（X轴下面）文字的区域
    private Rect _barRect, _bottomTxtRect;
    //X、Y轴的数据
    private List<String> _listX = new ArrayList<>();
    private List<Integer> _listY = new ArrayList<>();
    //柱状图底部（X轴下面）文字的高度
    private int _bottomTxtHeight = DpToPx(30);
    //柱状图顶部文字区域的高度
    private int _topTxtHeight = DpToPx(30);
    //柱状图的高度比
    private float _heightScale = 1;
    //触摸时按下的横向坐标
    private float _touchDownX = 0;
    //记录按下的时间，用来判断是否滑动
    private long _touchStartTime = 0;
    //滑动以后绘制的起始位置
    private float _afterScrollDraw = 0;
    //横向滑动一次的距离
    private float _hScrollDistance = 0;
    //是否正在滑动
    private boolean _isScrolling = false;

    public MyBarGraphView(Context context) {
        this(context, null);
    }

    public MyBarGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyBarGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        InitPaint();
    }

    private void InitPaint() {
        //柱状图顶部的文字
        _topTxtPaint = new Paint();
        _topTxtPaint.setTextSize(_topTxtSize);
        _topTxtPaint.setColor(_topTxtColor);
        _topTxtPaint.setStrokeCap(Paint.Cap.ROUND);
        _topTxtPaint.setStyle(Paint.Style.FILL);
        _topTxtPaint.setDither(true);
        //柱状图底部（X轴下面）的文字
        _bottomTxtPaint = new Paint();
        _bottomTxtPaint.setTextSize(_bottomTxtSize);
        _bottomTxtPaint.setColor(_bottomTxtColor);
        _bottomTxtPaint.setStrokeCap(Paint.Cap.ROUND);
        _bottomTxtPaint.setStyle(Paint.Style.FILL);
        _bottomTxtPaint.setDither(true);
        //柱状图
        _barPaint = new Paint();
        _barPaint.setTextSize(_topTxtSize);
        _barPaint.setColor(_barColor);
        _barPaint.setStrokeCap(Paint.Cap.ROUND);
        _barPaint.setStyle(Paint.Style.FILL);
        _barPaint.setDither(true);
        //X轴
        _xLinePaint = new Paint();
        _xLinePaint.setTextSize(_topTxtSize);
        _xLinePaint.setColor(_xLineColor);
        _xLinePaint.setStrokeCap(Paint.Cap.ROUND);
        _xLinePaint.setStyle(Paint.Style.FILL);
        _xLinePaint.setDither(true);
        //设置底部线的宽度
        _xLinePaint.setStrokeWidth(DpToPx(1f));
        //柱状图底部（X轴下面）的文字区域
        _bottomTxtRect = new Rect();
        //柱状图区域
        _barRect = new Rect();
    }

    public void SetData(List<String> listX, List<Integer> listY) {
        _listX = listX;
        _listY = listY;
        _heightScale = (float) GetMaxValue() / (float) (_height - _bottomTxtHeight - _topTxtHeight);
        invalidate();
    }

    /**
     * dp转px
     *
     * @param dp
     * @return
     */
    private int DpToPx(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * sp转px
     *
     * @param sp
     * @return
     */
    private int SpToPx(float sp) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * fontScale + 0.5f);
    }

    /**
     * 获取Y轴数据里最大的值，用于计算每个柱状图的高度
     *
     * @return
     */
    private int GetMaxValue() {
        int max = 0;
        if (_listY.size() > 0) {
            max = _listY.get(0);
            for (int i = 0; i < _listY.size(); i++) {
                if (_listY.get(i) > max) {
                    max = _listY.get(i);
                }
            }
        }
        return max;
    }

    /**
     * 获取屏幕尺寸信息
     *
     * @return
     */
    private ArrayList<Integer> GetScreenProperty() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        //屏幕宽度（像素）
        int width = dm.widthPixels;
        //屏幕高度（像素）
        int height = dm.heightPixels;
        //屏幕密度（0.75 / 1.0 / 1.5）
        float density = dm.density;
        //屏幕密度dpi（120 / 160 / 240）
        int densityDpi = dm.densityDpi;
        //屏幕宽度算法：屏幕宽度（像素） / 屏幕密度
        int screenWidth = (int) (width / density);
        int screenHeight = (int) (height / density);
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(screenWidth);
        integers.add(screenHeight);
        return integers;
    }

    /**
     * 获取超出当前屏幕以外部分的长度
     *
     * @return
     */
    private int GetScreenOutsideLength() {
        //（柱状图的宽度 + 柱状图之间的间隔） * 个数 - 屏幕宽度
        return (_barWidth + _barInterval) * _listX.size() - _screenWidth;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                _touchDownX = event.getX();
                _touchStartTime = System.currentTimeMillis();
                //点击的时候如果正在滑动则停止
                if (_isScrolling) {
                    removeCallbacks(_hScrollRunnable);
                    _isScrolling = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                //移动距离 = 滑动时的坐标 - 按下时的坐标
                float moveX = x - _touchDownX;
                _afterScrollDraw += moveX;
                Log.i(TAG, "moveX = " + moveX);
                Log.i(TAG, "_afterScrollDraw = " + _afterScrollDraw);
                //向右滑动
                if (moveX > 0) {
                    Log.i(TAG, "向右滑动");
                    if (_afterScrollDraw > 0) {
                        _afterScrollDraw = 0;
                    }
                }
                //向左滑动
                else {
                    Log.i(TAG, "向左滑动");
                    if (-_afterScrollDraw > GetScreenOutsideLength()) {
                        _afterScrollDraw = -GetScreenOutsideLength();
                    }
                }
                _hScrollDistance = moveX;
                //如果数据量少，没有充满横屏就没必要重新绘制
                if (GetScreenOutsideLength() > 0) {
                    invalidate();
                }
                _touchDownX = x;
                break;
            case MotionEvent.ACTION_UP:
                long endTime = System.currentTimeMillis();
                //滑动的速度如果大于某个值，并且要绘制的数据大于整个屏幕，才允许横向滑动
                float speed = _hScrollDistance / (endTime - _touchStartTime) * 1000;
                if (Math.abs(speed) > 100 && !_isScrolling && GetScreenOutsideLength() > 0) {
                    _hScrollRunnable = new HorizontalScrollRunnable(speed);
                    this.post(_hScrollRunnable);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    /**
     * 自定义View尺寸，比onDraw先执行
     * 计算过程参照父容器给出的大小以及自己的特点算出结果
     * <p>
     * EXACTLY：精确模式
     * 父容器能直接计算自定义控件的大小，一般是设置为match_parent或者固定值
     * <p>
     * AT_MOST：至多不超过模式
     * 父容器指定一个大小，自定义控件的大小不能超过这个值，父容器不能直接计算出自定义控件的大小，需要它自己计算，然后再去设置自定义控件的大小（setMeasuredDimension），一般是设置为warp_content
     * <p>
     * UNSPECIFIED：不确定模式
     * 父容器不对子View有任何限制，要多大给多大，多见于ListView、ScrollView、GridView等
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        if (widthMode == MeasureSpec.EXACTLY) {
            _screenWidth = width = widthSize;
        } else {
            width = GetScreenProperty().get(0);
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            _height = height = heightSize;
        } else {
            height = _height;
        }
        super.setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制X轴，Y方向距离加1个像素，避免X轴和柱状图有重叠
        canvas.drawLine(0, _height - _bottomTxtHeight + 1, _listX.size() * (_barWidth + _barInterval), _height - _bottomTxtHeight, _xLinePaint);
        //如果没有数据，绘制loading...
        if (_listY.size() == 0) {
            String txt = "loading...";
            float txtWidth = _bottomTxtPaint.measureText(txt);
            canvas.drawText(txt, _screenWidth / 2 - txtWidth / 2, _height / 2 - 10, _bottomTxtPaint);
        } else {
            //柱状图的横向起始位置
            int startX = (int) (_afterScrollDraw);
            //柱状图的纵向结束位置
            int stopY = _height - _bottomTxtHeight;
            for (int i = 0; i < _listX.size(); i++) {
                String xTxt = _listX.get(i);
                int yTxt = _listY.get(i);
                //每个柱状图的高度
                float barHeight = 0;
                if (_heightScale != 0) {
                    barHeight = (float) yTxt / _heightScale;
                }
                //柱状图的纵向起始位置
                int startY = (int) (_height - _bottomTxtHeight - barHeight);
                float topTxtWidth = _topTxtPaint.measureText(yTxt + "");
                //柱状图顶部文字的坐标
                float topTxtX = startX + _barWidth / 2 - topTxtWidth / 2;
                float topTxtY = startY - 20;
                canvas.drawText(yTxt + "", topTxtX, topTxtY, _topTxtPaint);
                _barRect.set(startX, startY, startX + _barWidth, stopY);
                canvas.drawRect(_barRect, _barPaint);
                float bottomTxtWidth = _bottomTxtPaint.measureText(xTxt);
                //柱状图底部（X轴下面）文字的坐标
                float bottomTxtX = startX + _barWidth / 2 - bottomTxtWidth / 2;
                _bottomTxtPaint.getTextBounds(xTxt, 0, xTxt.length(), _bottomTxtRect);
                float bottomTxtY = _height - _bottomTxtHeight + 20 + _bottomTxtRect.height();
                //绘制底部的文字
                canvas.drawText(xTxt, bottomTxtX, bottomTxtY, _bottomTxtPaint);
                //下一个柱状图开始绘制的位置
                startX = startX + _barWidth + _barInterval;
            }
        }
    }

}