package com.qrcode.qrcode.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by FengYang on 2016-06-03.
 */
public class ScanFrameView extends View {
    private static final String TAG = "ScanFrameView";
    //扫描框宽度
    private static final int FRAME_WIDTH = 280;
    //四个边角长度宽度
    private static final int CORNER_LENGTH = 10;
    private static final int CORNER_WIDTH = 2;
    //扫描线宽度
    private static final int SCAN_LINE_WIDTH = 10;
    //扫描框内边距
    private static final int FRAME_PADDING = 10;

    //屏幕密度
    private float screenDensity;
    //屏幕宽高
    private static int screenWidth;
    private static int screenHeight;
    //屏幕中心
    private Point screenCenter;
    //扫描框矩形
    private Rect frame;
    //画笔
    private Paint paint;


    public ScanFrameView(Context context) {
        this(context, null);
    }

    public ScanFrameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        screenDensity = context.getResources().getDisplayMetrics().density;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int widthCenter = screenWidth / 2;
        int heightCenter = screenHeight / 2;
        screenCenter = new Point(widthCenter, heightCenter);
        int top = heightCenter - dipToPx(FRAME_WIDTH / 2);
        int bottom = heightCenter + dipToPx(FRAME_WIDTH / 2);
        int left = widthCenter - dipToPx(FRAME_WIDTH / 2);
        int right = widthCenter + dipToPx(FRAME_WIDTH / 2);
        frame = new Rect(left, top, right, bottom);
        paint = new Paint();
        Log.d(TAG, widthCenter + "-" + heightCenter);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画遮盖区域
        paint.setARGB(125, 0, 0, 0);
        canvas.drawRect(0, 0, screenWidth, frame.top, paint);
        canvas.drawRect(0, frame.bottom, screenWidth, screenHeight, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right, frame.top, screenWidth, frame.bottom, paint);

        //画四个边角
        paint.setColor(Color.WHITE);
        int paddingPx = dipToPx(FRAME_PADDING);
        //左上
        canvas.drawRect(frame.left + paddingPx, frame.top + paddingPx,
                frame.left + paddingPx + dipToPx(CORNER_LENGTH),
                frame.top + paddingPx + dipToPx(CORNER_WIDTH), paint);
        canvas.drawRect(frame.left + paddingPx,
                frame.top + paddingPx + dipToPx(CORNER_WIDTH),
                frame.left + paddingPx + dipToPx(CORNER_WIDTH),
                frame.top + paddingPx + dipToPx(CORNER_LENGTH), paint);
        //右上
        canvas.drawRect(frame.right - paddingPx - dipToPx(CORNER_LENGTH),
                frame.top + paddingPx, frame.right - paddingPx,
                frame.top + paddingPx + dipToPx(CORNER_WIDTH), paint);
        canvas.drawRect(frame.right - paddingPx - dipToPx(CORNER_WIDTH),
                frame.top + paddingPx + dipToPx(CORNER_WIDTH),
                frame.right - paddingPx, frame.top + paddingPx + dipToPx(CORNER_LENGTH), paint);
        //左下
        canvas.drawRect(frame.left + paddingPx,
                frame.bottom - paddingPx - dipToPx(CORNER_LENGTH),
                frame.left + paddingPx + dipToPx(CORNER_WIDTH),
                frame.bottom - paddingPx, paint);
        canvas.drawRect(frame.left + paddingPx + dipToPx(CORNER_WIDTH),
                frame.bottom - paddingPx - dipToPx(CORNER_WIDTH),
                frame.left + paddingPx + dipToPx(CORNER_LENGTH),
                frame.bottom - paddingPx, paint);

        //右下
        canvas.drawRect(frame.right - paddingPx - dipToPx(CORNER_WIDTH),
                frame.bottom - paddingPx - dipToPx(CORNER_LENGTH),
                frame.right - paddingPx, frame.bottom - paddingPx, paint);
        canvas.drawRect(frame.right - paddingPx - dipToPx(CORNER_LENGTH),
                frame.bottom - paddingPx - dipToPx(CORNER_WIDTH),
                frame.right - paddingPx - dipToPx(CORNER_WIDTH),
                frame.bottom - paddingPx, paint);

        //绘制文字
        String text = "请把二维码或条形码放置在识别框中";
        paint.setAntiAlias(true);
        paint.setTextSize(dipToPx(12));
        float textWidth = paint.measureText(text);
        canvas.drawText(text, screenCenter.x - textWidth / 2, frame.top - dipToPx(20), paint);


    }

    private int dipToPx(int dip) {
        return (int) (dip * screenDensity + 0.5f);
    }

    public Rect getFrame() {
        return frame;
    }
}
