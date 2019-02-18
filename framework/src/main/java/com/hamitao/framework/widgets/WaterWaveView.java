package com.hamitao.framework.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import com.hamitao.framework.R;

/**
 * @data on 2018/5/11 18:02
 * @describe:
 */

public class WaterWaveView extends View {
    //边框宽度
    private int STROKE_WIDTH;
    //组件的宽，高
    private int width, height;
    /**
     * 进度条最大值和当前进度值
     */
    private float max;
    private int progress;

    /**
     * 绘制波浪的画笔
     */
    private Paint progressPaint;

    //波纹振幅与半径之比。(建议设置：<0.1)
    private static final float A = 0.05f;
    //绘制文字的画笔
    private Paint textPaint;
    //绘制边框的画笔
    private Paint circlePaint;

    /**
     * 圆弧圆心位置
     */
    private int centerX, centerY;

    //内圆所在的矩形
    private RectF circleRectF;

    public WaterWaveView(Context context) {
        super(context);
        init();
    }

    public WaterWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaterWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //初始化
    private void init() {
        progressPaint = new Paint();
        progressPaint.setColor(getResources().getColor(R.color.water_circle));//水纹颜色
        progressPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(getResources().getColor(R.color.water_grain));
        textPaint.setAntiAlias(true);

        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(getResources().getColor(R.color.water_grain));//外圈颜色

        autoRefresh();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (width == 0 || height == 0) {
            width = getWidth();
            height = getHeight();

            //计算圆弧半径和圆心点
            int circleRadius = Math.min(width, height) >> 1;
            STROKE_WIDTH = circleRadius / 10;
            circlePaint.setStrokeWidth(STROKE_WIDTH);

            centerX = width / 2;
            centerY = height / 2;

            VALID_RADIUS = circleRadius - STROKE_WIDTH;
            RADIANS_PER_X = (float) (Math.PI / VALID_RADIUS);
            circleRectF = new RectF(centerX - VALID_RADIUS, centerY - VALID_RADIUS,
                    centerX + VALID_RADIUS, centerY + VALID_RADIUS);
        }
    }

    private Rect textBounds = new Rect();

    //x方向偏移量
    private int xOffset;



//        bar.setMax(100);
//        bar.setProgressSync(50);
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制圆形边框
        canvas.drawCircle(centerX, centerY, VALID_RADIUS + (STROKE_WIDTH >> 1), circlePaint);
        //绘制水波曲线
        canvas.drawPath(getWavePath(xOffset), progressPaint);

        //绘制文字
        textPaint.setTextSize(VALID_RADIUS  );
        textPaint.setFakeBoldText(true);
        String text1 = String.valueOf(progress);
        //测量文字长度
        float w1 = textPaint.measureText(text1);
        //测量文字高度
        textPaint.getTextBounds("8", 0, 1, textBounds);
        float h1 = textBounds.height();
        float extraW = textPaint.measureText("8") / 3;
        canvas.drawText(text1, centerX - w1 / 2  , centerY + h1 / 2, textPaint);

//        textPaint.setTextSize(VALID_RADIUS / 6);
//        textPaint.getTextBounds("M", 0, 1, textBounds);
//        float h2 = textBounds.height();
//        canvas.drawText("M", centerX + w1 / 2 - extraW + 5, centerY - (h1 / 2 - h2), textPaint);

//        String text3 = "共" + String.valueOf(max) + "M";
//        String text3 = "";
//        float w3 = textPaint.measureText(text3, 0, text3.length());
//        textPaint.getTextBounds("M", 0, 1, textBounds);
//        float h3 = textBounds.height();
//        canvas.drawText(text3, centerX - w3 / 2, centerY + (VALID_RADIUS >> 1) + h3 / 2, textPaint);

//        String text4 = "";
//        float w4 = textPaint.measureText(text4, 0, text4.length());
//        textPaint.getTextBounds(text4, 0, text4.length(), textBounds);
//        float h4 = textBounds.height();
//        canvas.drawText(text4, centerX - w4 / 2, centerY - (VALID_RADIUS >> 1) + h4 / 2, textPaint);

    }

    //绘制水波的路径
    private Path wavePath;
    //每一个像素对应的弧度数
    private float RADIANS_PER_X;
    //去除边框后的半径（即内圆半径）
    private int VALID_RADIUS;

    /**
     * 获取水波曲线（包含圆弧部分）的Path.
     *
     * @param xOffset x方向像素偏移量.
     */
    private Path getWavePath(int xOffset) {
        if (wavePath == null) {
            wavePath = new Path();
        } else {
            wavePath.reset();
        }

        float[] startPoint = new float[2];  //波浪线起点
        float[] endPoint = new float[2];  //波浪线终点

        for (int i = 0; i <= VALID_RADIUS * 2; i += 2) {
            float x = centerX - VALID_RADIUS + i;
            float y = (float) (centerY + VALID_RADIUS * (1.0f + A) * 2 * (0.5f - progress / max)
                    + VALID_RADIUS * A * Math.sin((xOffset + i) * RADIANS_PER_X));

            //只计算内圆内部的点，边框上的忽略
            if (calDistance(x, y, centerX, centerY) > VALID_RADIUS) {
                if (x < centerX) {
                    continue;  //左边框,继续循环
                } else {
                    break; //右边框,结束循环
                }
            }

            //第1个点
            if (wavePath.isEmpty()) {
                startPoint[0] = x;
                startPoint[1] = y;
                wavePath.moveTo(x, y);
            } else {
                wavePath.lineTo(x, y);
            }

            endPoint[0] = x;
            endPoint[1] = y;
        }

        if (wavePath.isEmpty()) {
            if (progress / max >= 0.5f) {
                //满格
                wavePath.moveTo(centerX, centerY - VALID_RADIUS);
                wavePath.addCircle(centerX, centerY, VALID_RADIUS, Path.Direction.CW);
            } else {
                //空格
                return wavePath;
            }
        } else {
            //添加圆弧部分
            float startDegree = calDegreeByPosition(startPoint[0], startPoint[1]);  //0~180
            float endDegree = calDegreeByPosition(endPoint[0], endPoint[1]); //180~360
            wavePath.arcTo(circleRectF, endDegree - 360, startDegree - (endDegree - 360));
        }

        return wavePath;
    }

    private float calDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    //根据当前位置，计算出进度条已经转过的角度。
    private float calDegreeByPosition(float currentX, float currentY) {
        float a1 = (float) (Math.atan(1.0f * (centerX - currentX) / (currentY - centerY)) / Math.PI * 180);
        if (currentY < centerY) {
            a1 += 180;
        } else if (currentY > centerY && currentX > centerX) {
            a1 += 360;
        }

        return a1 + 90;
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }

    //直接设置进度值（同步）
    public void setProgressSync(int progress) {
        this.progress = progress;
        invalidate();
    }

    /**
     * 自动刷新页面，创造水波效果。组件销毁后该线城将自动停止。
     */
    private void autoRefresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!detached) {
                    xOffset += (VALID_RADIUS >> 4);
                    SystemClock.sleep(100);
                    postInvalidate();
                }
            }
        }).start();
    }

    //标记View是否已经销毁
    private boolean detached = false;

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        detached = true;
    }
}