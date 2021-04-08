package com.zistone.libble.controls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 仿遥控器的圆形按钮控件
 *
 * @author LiWei
 * @date 2020/9/14 9:12
 * @email 652276536@qq.com
 */
public class MyRemoteControlButton extends View {

    private static final String TAG = "MyRemoteControlButton";
    //背景色
    private static final int COLOR_BACK = Color.parseColor("#A9A9A9");
    //点击时的背景色
    private static final int COLOR_CLICK = Color.parseColor("#808080");
    //描边的颜色
    private static final int COLOR_STROKE = Color.parseColor("#FFFFFF");
    //描边的粗细
    private static final int SIZE_STROKE = 8;
    //半径的长度比
    private static final double DISTANCE_RADIUS = 0.4;

    //控件中心的坐标
    private int _centerX, _centerY;
    //扇形菜单列表
    private List<RoundMenu> _roundMenuList;
    //是否有中心按钮
    private boolean _isHaveCenterButton = false;
    //中心按钮的图片
    private Bitmap _centerBitmap;
    //中心按钮的点击事件
    private OnClickListener _centerOnClickListener;
    //偏移角度
    private float _offsetAngle;
    //点击状态，-2是无点击，-1是点击中心圆，其它是点击扇形菜单，这样定义方便和菜单列表的下标对应
    private int _clickState = -2;
    //中心圆的半径 = 大圆半径 * 半径长度比
    private int _centerRoundRadius;
    //按下时间，抬起的时候判断一下，超过这个时间算点击
    private long _touchTime;

    /**
     * 扇形的对象类
     */
    public static class RoundMenu {
        //绘制的扇形是否连接中心点
        public boolean isCenter = true;
        //图标
        public Bitmap bitmap;
        //点击事件
        public OnClickListener onClickListener;
        //图标距离中心点的距离
        public double iconDistance = 0.65;
    }

    public MyRemoteControlButton(Context context) {
        super(context);
    }

    public MyRemoteControlButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRemoteControlButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyRemoteControlButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 设置中心圆按钮
     *
     * @param bitmap
     */
    public void SetCenterButton(Bitmap bitmap) {
        //该方法被调用则说明添加了中心圆按钮，所以要将状态改变
        _isHaveCenterButton = true;
        _centerBitmap = bitmap;
    }

    /**
     * 设置中心圆按钮的点击事件
     *
     * @param onClickListener
     */
    public void SetCenterButtonListener(OnClickListener onClickListener) {
        _centerOnClickListener = onClickListener;
    }

    /**
     * 添加扇形菜单
     *
     * @param roundMenu
     */
    public void AddRoundMenu(RoundMenu roundMenu) {
        if (null == roundMenu)
            return;
        if (null == _roundMenuList)
            _roundMenuList = new ArrayList<>();
        _roundMenuList.add(roundMenu);
    }

    /**
     * 求两条线的夹角
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    private int GetTwoLineAngle(float x1, float y1, float x2, float y2) {
        double angle = 0;
        double k1 = (double) (y1 - y1) / (x1 * 2 - x1);
        double k2 = (double) (y2 - y1) / (x2 - x1);
        //正切值
        double tempAngle = Math.atan((Math.abs(k1 - k2)) / (1 + k1 * k2)) / Math.PI * 180;
        //第一象现
        if (x2 > x1 && y2 < y1)
            angle = 90 - tempAngle;
            //第二象现
        else if (x2 > x1 && y2 > y1)
            angle = 90 + tempAngle;
            //第三象现
        else if (x2 < x1 && y2 > y1)
            angle = 270 - tempAngle;
            //第四象现
        else if (x2 < x1 && y2 < y1)
            angle = 270 + tempAngle;
        else if (x2 == x1 && y2 < y1)
            angle = 0;
        else if (x2 == x1 && y2 > y1)
            angle = 180;
        return (int) angle;
    }

    /**
     * 求两个坐标之间的直线距离
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    private double GetTwoPointDistance(float x1, float y1, float x2, float y2) {
        float width, height;
        if (x1 > x2)
            width = x1 - x2;
        else
            width = x2 - x1;
        if (y1 > y2)
            height = y1 - y2;
        else
            height = y2 - y1;
        return Math.sqrt(width * width + height * height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        _centerX = getWidth() / 2;
        _centerY = getHeight() / 2;
        //绘制扇形菜单
        if (null != _roundMenuList && !_roundMenuList.isEmpty()) {
            //中心圆的半径 = 大圆半径 * 半径长度比
            _centerRoundRadius = (int) (_centerX * DISTANCE_RADIUS);
            RectF rectF = new RectF(0, 0, getWidth(), getHeight());
            //根据菜单列表计算每个弧的角度，这个角度是没有偏移的
            float everyAngle = 360 / _roundMenuList.size();
            //偏移角度，偏移后扇形就能绘制成“X”形状，否则是“+”形状
            _offsetAngle = everyAngle / 2;
            Log.i(TAG, "偏移角度：" + _offsetAngle);
            //每个扇形的实际角度，也就是偏移后的角度，用于计算扇形中心位置
            float offsetAfterAngle;
            for (int i = 0; i < _roundMenuList.size(); i++) {
                RoundMenu roundMenu = _roundMenuList.get(i);
                //绘制
                Paint paint = new Paint();
                //抗锯齿
                paint.setAntiAlias(true);
                //点击时的颜色
                if (_clickState == i)
                    paint.setColor(COLOR_CLICK);
                else
                    paint.setColor(COLOR_BACK);
                //绘制圆弧
                canvas.drawArc(rectF, _offsetAngle + i * everyAngle, everyAngle, true, paint);
                offsetAfterAngle = ((i + 1) * 2 - 1) * (everyAngle / 2);
                Log.i(TAG, "扇形[" + i + "]的1/2夹角：" + offsetAfterAngle);
                //绘制边
                paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStrokeWidth(SIZE_STROKE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(COLOR_STROKE);
                canvas.drawArc(rectF, _offsetAngle + i * everyAngle, everyAngle, roundMenu.isCenter, paint);
                //绘制扇形图标
                //如果菜单共用一个图标使用下面代码即可，已做旋转处理
                float bitmapX = (float) ((_centerX + getWidth() / 2 * roundMenu.iconDistance) - (roundMenu.bitmap.getWidth() / 2));
                float bitmapY = _centerY - (roundMenu.bitmap.getHeight() / 2);
                Matrix matrix = new Matrix();
                matrix.postTranslate(bitmapX, bitmapY);
                matrix.postRotate((i + 1) * everyAngle, _centerX, _centerY);
                canvas.drawBitmap(roundMenu.bitmap, matrix, null);
            }
        }
        //绘制中心圆
        if (_isHaveCenterButton) {
            //填充
            RectF rectF = new RectF(_centerX - _centerRoundRadius, _centerY - _centerRoundRadius, _centerX + _centerRoundRadius,
                    _centerY + _centerRoundRadius);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(SIZE_STROKE);
            //点击时的颜色
            if (_clickState == -1)
                paint.setColor(COLOR_CLICK);
            else
                paint.setColor(COLOR_BACK);
            canvas.drawArc(rectF, 0, 360, true, paint);
            //绘制边线
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(SIZE_STROKE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(COLOR_STROKE);
            canvas.drawArc(rectF, 0, 360, true, paint);
            //绘制中心圆的图标
            if (null != _centerBitmap) {
                canvas.drawBitmap(_centerBitmap, _centerX - _centerBitmap.getWidth() / 2, _centerY - _centerBitmap.getHeight() / 2, null);
            }
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                _touchTime = System.currentTimeMillis();
                //判断点击的区域是否在在中心圆的范围
                float textX = event.getX();
                float textY = event.getY();
                int distanceLine = (int) GetTwoPointDistance(_centerX, _centerY, textX, textY);
                //按下的点到中心点距离小于中心圆半径，那就是点击中心圆了
                if (distanceLine <= _centerRoundRadius) {
                    _clickState = -1;
                }
                //按下的点到中心点的距离大于中心圆半径小于大圆半径，那就是点击的某个扇形了
                else if (distanceLine <= getWidth() / 2) {
                    //根据菜单列表计算每个弧的角度
                    float sweepAngle = 360 / _roundMenuList.size();
                    //计算夹角
                    int angle = GetTwoLineAngle(_centerX, _centerY, textX, textY);
                    //计算出来的夹角是从正Y轴开始，而我们的扇形是从正X轴开始，再加上偏移角度
                    angle = (angle + 360 - 90 - (int) _offsetAngle) % 360;
                    //根据夹角得出点击的是哪个扇形
                    _clickState = (int) (angle / sweepAngle);
                }
                //点击了控件外面
                else {
                    _clickState = -2;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //小于300毫秒算点击
                if (System.currentTimeMillis() - _touchTime < 300) {
                    OnClickListener onClickListener = null;
                    //点击的中心圆
                    if (_clickState == -1) {
                        onClickListener = _centerOnClickListener;
                    }
                    //点击的扇形
                    else if (_clickState >= 0 && _clickState < _roundMenuList.size()) {
                        onClickListener = _roundMenuList.get(_clickState).onClickListener;
                    }
                    if (null != onClickListener) {
                        onClickListener.onClick(this);
                    }
                }
                _clickState = -2;
                invalidate();
                break;
        }
        return true;
    }

}
