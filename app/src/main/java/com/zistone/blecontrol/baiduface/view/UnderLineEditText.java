package com.zistone.blecontrol.baiduface.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

public class UnderLineEditText extends AppCompatEditText {
    private Paint paint;
    private static final int defaultLength = 2;

    public UnderLineEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 设置画笔的属性
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        // 设置画笔颜色为红色
        paint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /**canvas画直线，从左下角到右下角，this.getHeight()-2是获得父edittext的高度，但是必须要-2这样才能保证
         * 画的横线在edittext上面，和原来的下划线的重合
         */
        canvas.drawLine(0, this.getHeight() - defaultLength, this.getWidth() - defaultLength,
                this.getHeight() - defaultLength, paint);
    }
}