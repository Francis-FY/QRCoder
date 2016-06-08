package com.qrcode.qrcode.decode;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.qrcode.qrcode.BuildConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

/**
 * Created by FengYang on 2016-06-03.
 */
public class DecodeThread extends Thread {

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private Handler handler;
    private final Hashtable<DecodeHintType, Object> hints;
    private Camera.Size size;
    private Rect rect;
    private Handler fragementHandler;
    private Point screenSize;

    public DecodeThread(Camera.Size size,Point screenSize, Handler fragHandler, Rect rect){

        this.size = size;
        this.rect = rect;
        this.fragementHandler = fragHandler;
        this.screenSize = screenSize;

        hints = new Hashtable<DecodeHintType, Object>(3);
        Vector<BarcodeFormat> decodeFormats = new Vector<>();
        decodeFormats.add(BarcodeFormat.QR_CODE);
        decodeFormats.add(BarcodeFormat.EAN_13);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
    }

    public Handler getHandler() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(size,screenSize,rect,fragementHandler,hints);
        countDownLatch.countDown();
        Looper.loop();
    }
}
